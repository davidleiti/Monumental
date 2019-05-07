package ubb.thesis.david.navigationdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnSuccessListener<Location> {

    private var currentDegree: Float = 0F
    private var navigator: Navigator? = null

    private val locationRequest =
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30 * 1000
            fastestInterval = 5 * 1000
        }

    private lateinit var locationUpdateCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initCallback()
        enableMyLocation()
    }

    private fun initCallback() {
        locationUpdateCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                navigator?.updateLocation(locationResult.lastLocation) ?: run {
                    navigator = FusedNavigator(this@MainActivity, locationResult.lastLocation).apply {
                        setListener(navigatorListener())
                        target = defaultTarget()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigator?.start()
        requestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        navigator?.stop()
        disableLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_MY_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                onMyLocationEnabled()
            } else {
                enableMyLocation()
            }
        } else if (requestCode == RC_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                onLocationEnabled()
            } else {
                requestGpsSettings()
            }
        }
    }

    override fun onSuccess(location: Location?) {
        Log.d(TAG, "Location retrieved successfully!")
        Toast.makeText(this, "Location retrieved", Toast.LENGTH_SHORT).show()

        location?.let {
            navigator?.updateLocation(location) ?: run {
                navigator = FusedNavigator(this, it).apply {
                    setListener(navigatorListener())
                    target = defaultTarget()
                }
            }
        }
    }

    private fun requestLocationUpdates() {
        if (checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(
                            locationRequest,
                            locationUpdateCallback,
                            null
                    )
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), RC_MY_LOCATION)
        }
    }

    private fun disableLocationUpdates() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationUpdateCallback)
    }

    private fun navigatorListener(): Navigator.OnHeadingChangedListener =
        object : Navigator.OnHeadingChangedListener {

            @SuppressLint("SetTextI18n")
            override fun onChanged(direction: Float) {
                label_direction.text = "Degrees to target: ${direction.toInt()}°"
                RotateAnimation(currentDegree, direction,
                                Animation.RELATIVE_TO_SELF, 0.5F,
                                Animation.RELATIVE_TO_SELF, 0.5F)
                        .also { anim ->
                            anim.duration = 500
                            anim.repeatCount = 0
                            anim.fillAfter = true
                            arrow_orientation.startAnimation(anim)
                        }

                currentDegree = direction
            }

        }

    private fun requestGpsSettings() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(locationSettingsRequest)
                .apply {
                    addOnSuccessListener { onLocationEnabled() }
                    addOnFailureListener(this@MainActivity) { error ->
                        if (error is ResolvableApiException) {
                            try {
                                error.startResolutionForResult(this@MainActivity, RC_SETTINGS)
                            } catch (sendException: IntentSender.SendIntentException) {
                                // Ignore this error as suggested in the documentation
                            }
                        }
                    }
                }
    }

    private fun enableMyLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            onMyLocationEnabled()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), RC_MY_LOCATION)
        }
    }

    private fun onMyLocationEnabled() {
        Toast.makeText(this, "My location is enabled", Toast.LENGTH_SHORT).show()
        requestGpsSettings()
    }

    private fun onLocationEnabled() {
        Toast.makeText(this, "Location is enabled", Toast.LENGTH_SHORT).show()
        requestLocationUpdates()
    }

    private fun defaultTarget(): Location = Location("").apply {
        latitude = 46.777366
        longitude = 23.615983
    }

    companion object {
        private const val TAG = "MainLogger"
        private const val RC_MY_LOCATION = 10
        private const val RC_SETTINGS = 20
    }
}
