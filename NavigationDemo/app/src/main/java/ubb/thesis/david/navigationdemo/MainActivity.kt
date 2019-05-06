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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnSuccessListener<Location> {

    private var currentDegree: Float = 0F
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestGpsSettings(RC)
        navigator = FusedNavigator(this).apply {
            setListener(getNavigatorListener())
        }

        seek_target.setOnClickListener {
            requestLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        navigator.start()
    }

    override fun onPause() {
        super.onPause()
        navigator.stop()
    }

    private fun getNavigatorListener(): Navigator.OnHeadingChangedListener =
        object : Navigator.OnHeadingChangedListener {
            @SuppressLint("SetTextI18n")
            override fun onChanged(direction: Float) {
                label_direction.text = "Degrees to target: ${direction.toInt()} degrees"
                val rotateAnimation =
                    RotateAnimation(currentDegree, direction,
                                    Animation.RELATIVE_TO_SELF, 0.5F,
                                    Animation.RELATIVE_TO_SELF, 0.5F)
                            .apply {
                                duration = 500
                                repeatCount = 0
                                fillAfter = true
                            }

                arrow_orientation.startAnimation(rotateAnimation)

                val sign = if (navigator.hasTarget()) 1 else -1
                currentDegree = direction * sign
            }
        }

    private fun requestGpsSettings(requestCode: Int) {
        val enableLocationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30 * 1000
            fastestInterval = 5 * 1000
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(enableLocationRequest)
                .setAlwaysShow(true)
                .build()

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(locationSettingsRequest)
                .apply {
                    addOnSuccessListener { onLocationEnabled() }
                    addOnFailureListener(this@MainActivity) { error ->
                        if (error is ResolvableApiException) {
                            try {
                                error.startResolutionForResult(this@MainActivity, requestCode)
                            } catch (sendException: IntentSender.SendIntentException) {
                                // Ignore this error as suggested in the documentation
                            }
                        }
                    }
                }
    }

    private fun enableMyLocation() {
        if (checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            onMyLocationEnabled()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), RC2)
        }
    }

    private fun requestLocation() {
        if (checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                    .addOnSuccessListener(this)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), RC2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC) {
            if (resultCode == Activity.RESULT_OK) {
                onLocationEnabled()
            } else {
                requestGpsSettings(RC)
            }
        } else if (requestCode == RC2) {
            if (resultCode == Activity.RESULT_OK) {
                onMyLocationEnabled()
            } else {
                enableMyLocation()
            }
        }
    }

    override fun onSuccess(location: Location) {
        Log.d(TAG, "Location retrieved successfully!")
        Toast.makeText(this, "Location retrieved", Toast.LENGTH_SHORT).show()

        val target = Location("").apply {
            latitude = 46.777366
            longitude = 23.615983
        }
        navigator.setEndpoints(location, target)
    }

    private fun onLocationEnabled() {
        Toast.makeText(this, "Location is enabled", Toast.LENGTH_SHORT).show()
        enableMyLocation()
    }

    private fun onMyLocationEnabled() {
        seek_target.isEnabled = true
        Toast.makeText(this, "My location is enabled", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainLogger"
        private const val RC = 10
        private const val RC2 = 20
    }
}
