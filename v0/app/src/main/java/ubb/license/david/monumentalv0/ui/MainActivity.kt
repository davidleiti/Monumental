package ubb.license.david.monumentalv0.ui

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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_overlay.*
import ubb.license.david.monumentalv0.GeofencingClientWrapper
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.fadeIn
import ubb.license.david.monumentalv0.utils.fadeOut
import ubb.license.david.monumentalv0.utils.info

class MainActivity : AppCompatActivity(), UiActions, ClientProvider,
                     NavigationView.OnNavigationItemSelectedListener,
                     GoogleApiClient.OnConnectionFailedListener,
                     GoogleApiClient.ConnectionCallbacks {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mGoogleApiClient: GoogleApiClient by lazy { initializeGoogleApiClient() }
    private val mGoogleSignInClient: GoogleSignInClient by lazy { initializeGoogleSignInClient() }
    private val mGeofencingClient: GeofencingClientWrapper by lazy { GeofencingClientWrapper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAppBar()
        title = ""
    }

    private fun setupAppBar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close).apply { syncState() }

        drawer_layout.addDrawerListener(toggle)
        nav_view.setNavigationItemSelectedListener(this)
        disableUserNavigation()
    }

    override fun getAuth(): FirebaseAuth = firebaseAuth

    override fun getGoogleSignInClient(): GoogleSignInClient = mGoogleSignInClient

    override fun getGeofencingClient(): GeofencingClientWrapper = mGeofencingClient

    override fun getGoogleApiClient(): GoogleApiClient {
        if (!mGoogleApiClient.isConnected)
            mGoogleApiClient.connect()
        return mGoogleApiClient
    }

    override fun showLoading() = progress_overlay.fadeIn()

    override fun hideLoading() = progress_overlay.fadeOut()

    override fun enableUserNavigation() {
        supportActionBar?.show()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun disableUserNavigation() {
        supportActionBar?.hide()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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
        when (item.itemId) {
            R.id.option_sign_out -> {
                signOut()
                navigateToLogin()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        nav_host_fragment.childFragmentManager.fragments[0].onActivityResult(requestCode, resultCode, data)

    private fun navigateToLogin() {
        val fragmentView = nav_host_fragment.childFragmentManager.fragments[0].view
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.startDestination, true)
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .build()
        print("Am facut licenta")
        Navigation.findNavController(fragmentView!!)
            .navigate(R.id.loginDestination, null, options)
    }

    override fun onConnected(p0: Bundle?) {
        info(TAG_LOG, "GoogleApiClient connection successful!")
    }

    override fun onConnectionSuspended(p0: Int) {
        debug(TAG_LOG, "GoogleApiClient connection suspended!")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        debug(TAG_LOG, "Failed to connect to the GoogleApiClient, cause: ${p0.errorMessage}")
    }

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
        mGoogleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        private const val TAG_LOG = "MainLogger"
    }
}
