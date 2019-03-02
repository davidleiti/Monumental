package ubb.license.david.monumentalv0.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_home.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.info
import ubb.license.david.monumentalv0.utils.longToast

class MainActivity : ProgressOverlayActivity(), NavigationView.OnNavigationItemSelectedListener,
                     LocationCallbacks, GoogleApiClient.OnConnectionFailedListener,
                     GoogleApiClient.ConnectionCallbacks {

    private val mGoogleApiClient: GoogleApiClient by lazy { initializeGoogleApiClient() }
    private val logTag = "MainLogger"

    override fun getGoogleApiClient(): GoogleApiClient {
        if (!mGoogleApiClient.isConnected)
            mGoogleApiClient.connect()
        return mGoogleApiClient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        title = ""

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.option_sign_out -> {
                val launchLoginIntent = Intent(this, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_SIGNED_OUT, true)
                }
                startActivity(launchLoginIntent)
                finish()
            }
        }
        return true
    }

    override fun onConnected(p0: Bundle?) {
        info(logTag, "GoogleApiClient connection successful!")
    }

    override fun onConnectionSuspended(p0: Int) {
        debug(logTag, "GoogleApiClient connection suspended!")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        debug(logTag, "Failed to connect to the GoogleApiClient, cause: ${p0.errorMessage}")
        longToast("Failed to connect to map services!")
    }

    private fun initializeGoogleApiClient(): GoogleApiClient =
        GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
}
