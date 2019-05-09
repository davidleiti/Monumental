package ubb.thesis.david.monumental

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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import kotlinx.android.synthetic.main.progress_overlay.*
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.common.ClientProvider
import ubb.thesis.david.monumental.common.UiActions
import ubb.thesis.david.monumental.utils.fadeIn
import ubb.thesis.david.monumental.utils.fadeOut

class HostActivity : AppCompatActivity(), UiActions, ClientProvider,
    NavigationView.OnNavigationItemSelectedListener,
    GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleApiClient: GoogleApiClient by lazy { initializeGoogleApiClient() }
    private val googleSignInClient: GoogleSignInClient by lazy { initializeGoogleSignInClient() }
    private val geofencingClientAdapter: GeofencingClientAdapter by lazy {
        GeofencingClientAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAppBar()
        title = ""
    }

    private fun setupAppBar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                                           R.string.navigation_drawer_open,
                                           R.string.navigation_drawer_close).apply { syncState() }

        drawer_layout.addDrawerListener(toggle)
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun getAuth(): FirebaseAuth = firebaseAuth

    override fun getUserId(): String = firebaseAuth.currentUser!!.uid

    override fun getSignInClient(): GoogleSignInClient = googleSignInClient

    override fun getGeofencingClient(): GeofencingClientAdapter = geofencingClientAdapter

    override fun getApiClient(): GoogleApiClient {
        if (!googleApiClient.isConnected)
            googleApiClient.connect()
        return googleApiClient
    }

    override fun showLoading() = progress_overlay.fadeIn()

    override fun hideLoading() = progress_overlay.fadeOut()

    override fun enableUserNavigation() {
        supportActionBar?.show()
        val shouldInit = drawer_layout.getDrawerLockMode(nav_view) != DrawerLayout.LOCK_MODE_UNLOCKED
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        if (shouldInit)     // Only populate drawer views if the drawer has been previously hidden
            initDrawer()
    }

    override fun disableUserNavigation() {
        supportActionBar?.hide()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (progress_overlay.visibility == View.GONE)
                super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        return when (item.itemId) {
            R.id.option_sign_out -> {
                geofencingClientAdapter.removeBeacons(collectionId = firebaseAuth.currentUser!!.uid)
                signOut()
                navigateToLogin()
                false
            }
            else -> true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        nav_host_fragment.childFragmentManager.fragments[0].onActivityResult(requestCode, resultCode, data)

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

    override fun onConnected(p0: Bundle?) =
        info(TAG_LOG, "GoogleApiClient connection successful!")

    override fun onConnectionSuspended(p0: Int) =
        debug(TAG_LOG, "GoogleApiClient connection suspended!")

    override fun onConnectionFailed(p0: ConnectionResult) =
        debug(TAG_LOG, "Failed to connect to the GoogleApiClient, cause: ${p0.errorMessage}")

    private fun initializeGoogleApiClient(): GoogleApiClient =
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

    private fun initializeGoogleSignInClient() =
        GoogleSignIn.getClient(this,
                               GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                       .requestIdToken(getString(R.string.default_web_client_id))
                                       .requestEmail()
                                       .build())

    private fun signOut() {
        getAuth().signOut()
        googleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        private const val TAG_LOG = "MainLogger"
    }
}
