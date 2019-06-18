package ubb.thesis.david.monumental.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import kotlinx.android.synthetic.main.progress_overlay.*
import ubb.thesis.david.data.FirebaseAuthenticatorAdapter
import ubb.thesis.david.data.FirebaseDataAdapter
import ubb.thesis.david.data.FirebaseLandmarkDetector
import ubb.thesis.david.data.FirebaseStorageAdapter
import ubb.thesis.david.domain.*
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.ClientProvider
import ubb.thesis.david.monumental.geofencing.GeofencingClientAdapter
import ubb.thesis.david.monumental.utils.fadeIn
import ubb.thesis.david.monumental.utils.fadeOut

class HostActivity : AppCompatActivity(), FragmentHostActions, ClientProvider,
    NavigationView.OnNavigationItemSelectedListener {

    // Authentication clients
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient by lazy { initializeGoogleSignInClient() }

    // Interface adapter instances
    private val geofencingClientAdapter: BeaconManager by lazy { GeofencingClientAdapter(this) }
    private val firebaseDataAdapter: CloudDataSource by lazy { FirebaseDataAdapter() }
    private val firebaseStorageAdapter: ImageStorage by lazy { FirebaseStorageAdapter() }
    private val firebaseAuthenticationAdapter: UserAuthenticator by lazy { FirebaseAuthenticatorAdapter() }
    private val firebaseLandmarkDetector: LandmarkDetector by lazy { FirebaseLandmarkDetector(this) }

    // Startup flag
    var shouldDisplaySplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAppBar()
        title = ""
        if (intent.hasExtra(KEY_LAUNCH_AT_DESTINATION))
            shouldDisplaySplash = false
    }

    override fun onStart() {
        super.onStart()
        handleIntent(intent)
        shouldDisplaySplash = false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activeFragments = nav_host_fragment.childFragmentManager.fragments
        if (activeFragments.size > 0)
            activeFragments[0].onActivityResult(requestCode, resultCode, data)
        else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (progress_overlay.visibility == View.GONE)
                super.onBackPressed()
        }
    }

    // ClientProvider actions
    override fun getBeaconManager(): BeaconManager = geofencingClientAdapter
    override fun getDataSource(): CloudDataSource = firebaseDataAdapter
    override fun getImageStorage(): ImageStorage = firebaseStorageAdapter
    override fun getUserAuthenticator(): UserAuthenticator = firebaseAuthenticationAdapter
    override fun getLandmarkDetector(): LandmarkDetector = firebaseLandmarkDetector
    override fun getSignInClient(): GoogleSignInClient = googleSignInClient

    // FragmentHostActions
    override fun displayProgress() = progress_overlay.fadeIn()
    override fun hideProgress() = progress_overlay.fadeOut()

    override fun setTitle(text: String?) {
        text?.let { toolbar_title.text = it } ?: run { toolbar_title.text = "" }
    }

    override fun enableUserNavigation() {
        supportActionBar?.show()

        // Initialize drawer layout components if it has been hidden previously
        if (drawer_layout.getDrawerLockMode(nav_view) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            initDrawer()

        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun disableUserNavigation() {
        supportActionBar?.hide()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun getUserId(): String? = firebaseAuth.currentUser?.uid

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.hasExtra(KEY_LAUNCH_AT_DESTINATION)) {
                when (it.getIntExtra(KEY_LAUNCH_AT_DESTINATION, 0)) {
                    DESTINATION_NAVIGATION -> navigateToSession()
                }
            }
        }
    }

    private fun setupAppBar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                                           R.string.navigation_drawer_open,
                                           R.string.navigation_drawer_close).apply { syncState() }

        drawer_layout.addDrawerListener(toggle)
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun initDrawer() {
        firebaseAuth.currentUser?.run {
            with(nav_view) {
                label_header_email.text = email
                if (displayName != null && displayName != "") {
                    label_header_name.visibility = View.VISIBLE
                    label_header_name.text = displayName
                } else {
                    label_header_name.visibility = View.GONE
                }

                photoUrl?.let { url ->
                    image_header_profile.clipToOutline = true
                    Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.ic_account_circle_white_24dp)
                            .into(image_header_profile)
                } ?: run {
                    image_header_profile.setImageResource(R.drawable.ic_account_circle_white_24dp)
                }
            }
        }
    }

    // Setup navigation options
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        return when (item.itemId) {
            R.id.option_sign_out -> {
                geofencingClientAdapter.wipeBeacons(collectionId = firebaseAuth.currentUser!!.uid)
                signOut()
                navigateToLogin()
                false
            }
            R.id.option_history -> {
                navigateToHistory()
                false
            }
            else -> false
        }
    }

    private fun navigateToSession() {
        val fragmentView = nav_host_fragment.childFragmentManager.fragments[0].view
        val options = NavOptions.Builder()
                .setPopUpTo(R.id.startDestination, false)
                .setEnterAnim(R.anim.fade_in_bottom)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.fade_out_bottom)
                .build()
        Navigation.findNavController(fragmentView!!).navigate(R.id.nav_session, null, options)
    }

    private fun navigateToLogin() {
        val fragmentView = nav_host_fragment.childFragmentManager.fragments[0].view
        val options = NavOptions.Builder()
                .setPopUpTo(R.id.startDestination, true)
                .setEnterAnim(R.anim.fade_in_bottom)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .build()
        print("Am facut licenta")
        Navigation.findNavController(fragmentView!!).navigate(R.id.loginDestination, null, options)
    }

    private fun navigateToHistory() {
        val fragmentView = nav_host_fragment.childFragmentManager.fragments[0].view
        val options = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in_right)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.fade_out_right)
                .build()
        Navigation.findNavController(fragmentView!!).navigate(R.id.nav_history, null, options)
    }

    private fun initializeGoogleSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(this, options)
    }

    private fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        const val KEY_LAUNCH_AT_DESTINATION = "NavigateToSpecifiedDestination"
        const val DESTINATION_NAVIGATION = 1
    }
}
