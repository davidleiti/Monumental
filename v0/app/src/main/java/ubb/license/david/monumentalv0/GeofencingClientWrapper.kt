package ubb.license.david.monumentalv0

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import ubb.license.david.monumentalv0.services.GeofenceTransitionsService
import ubb.license.david.monumentalv0.utils.checkPermission

class GeofencingClientWrapper(private val context: Context) {

    private val client = LocationServices.getGeofencingClient(context)
    private val transitionsServiceIntent: PendingIntent? by lazy {
        val geofencingServiceIntent = Intent(context, GeofenceTransitionsService::class.java)
        PendingIntent.getService(context, 0, geofencingServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun createGeofence(id: String, lat: Double, lng: Double,
                       onSuccess: () -> Unit,
                       onFailure: (errorMessage: String?) -> Unit) {
        buildGeofence(id, lat, lng)?.let { geofence ->
            if (context.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                client.addGeofences(buildGeofencingRequest(geofence), transitionsServiceIntent)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message) }
            }
        }
    }

    fun removeGeofence(id: String,
                       onSuccess: () -> Unit,
                       onFailure: (errorMessage: String?) -> Unit) =
        client.removeGeofences(listOf(id))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest? =
        GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(0)
            .build()

    private fun buildGeofence(id: String, lat: Double, lng: Double): Geofence? =
        Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lng, DEFAULT_RADIUS)
            .setLoiteringDelay(DWELLING_DELAY)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

    companion object {
        private const val DEFAULT_RADIUS = 100F
        private const val DWELLING_DELAY = 20_000
    }

}