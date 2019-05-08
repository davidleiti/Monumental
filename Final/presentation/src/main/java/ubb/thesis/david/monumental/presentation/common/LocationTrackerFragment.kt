package ubb.thesis.david.monumental.presentation.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import ubb.thesis.david.monumental.utils.checkPermission

abstract class LocationTrackerFragment : BaseFragment() {

    private var updateCallback: LocationCallback? = null
    private var locationRetrievedListener: OnSuccessListener<Location>? = null

    private val updateRequest: LocationRequest by lazy { createLocationRequest() }
    private val locationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(activity!!)
    }

    protected abstract fun createLocationRequest(): LocationRequest

    protected fun requestLastLocation(onSuccessListener: OnSuccessListener<Location>) {
        locationRetrievedListener = onSuccessListener
        prepareLocation()
    }

    protected fun requestLocationUpdates(locationCallback: LocationCallback) {
        updateCallback = locationCallback
        prepareLocation()
    }

    protected fun disableLocationUpdates() {
        updateCallback ?: LocationServices.getFusedLocationProviderClient(activity!!)
                .removeLocationUpdates(updateCallback)
        updateCallback = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_LOCATION_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK) {
                    requestEnableLocation()
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                       RC_LOCATION_PERMISSION)
                }
            }
            RC_ENABLE_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    onLocationPrepared()
                } else {
                    requestEnableLocation()
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun prepareLocation() {
        if (context!!.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestEnableLocation()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                               RC_LOCATION_PERMISSION)
        }
    }

    private fun requestEnableLocation() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(updateRequest)
                .setAlwaysShow(true)
                .build()

        LocationServices.getSettingsClient(activity!!)
                .checkLocationSettings(locationSettingsRequest)
                .apply {
                    addOnSuccessListener { onLocationPrepared() }
                    addOnFailureListener(activity!!) { error ->
                        if (error is ResolvableApiException) {
                            try {
                                error.startResolutionForResult(activity!!, RC_ENABLE_LOCATION)
                            } catch (sendException: IntentSender.SendIntentException) {
                                // Ignore this error as suggested in the documentation
                            }
                        }
                    }
                }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPrepared() {
        locationRetrievedListener?.let {
            locationProviderClient.lastLocation.addOnSuccessListener(locationRetrievedListener!!)
            locationRetrievedListener = null
        } ?: run {
            locationProviderClient.requestLocationUpdates(updateRequest, updateCallback, null)
        }
    }

    companion object {
        private const val RC_LOCATION_PERMISSION = 10
        private const val RC_ENABLE_LOCATION = 20
    }

}