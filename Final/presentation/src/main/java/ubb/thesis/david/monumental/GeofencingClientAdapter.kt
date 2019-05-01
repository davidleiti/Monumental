package ubb.thesis.david.monumental

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.monumental.geofencing.GeofenceBroadcastReceiver
import ubb.thesis.david.monumental.utils.checkPermission

class GeofencingClientAdapter(private val context: Context) : BeaconManager {

    private val client = LocationServices.getGeofencingClient(context)
    private val transitionTriggerIntent: PendingIntent? by lazy {
        val geofencingServiceIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, geofencingServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun setupBeacon(id: String, lat: Double, lng: Double, collectionId: String) {
        val storage = context.getSharedPreferences(collectionId, Context.MODE_PRIVATE)

        buildGeofence(id, lat, lng)?.let { geofence ->
            if (context.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                client.addGeofences(buildGeofencingRequest(geofence), transitionTriggerIntent)
                        .addOnSuccessListener {
                            info(TAG_LOG, "Created and registered geofence $id")
                            storage.edit {
                                putBoolean(id, true)
                            }
                        }
                        .addOnFailureListener {
                            debug(
                                    TAG_LOG, "Failed to create geofence $id, cause: $it")
                        }
            }
        }
    }

    override fun removeBeacons(collectionId: String) {
        val storage = context.getSharedPreferences(collectionId, Context.MODE_PRIVATE)
        storage.edit().run {
            for (entry in storage.all) {
                remove(entry.key)
                removeGeofence(entry.key,
                               onSuccess = {
                                   info(
                                           TAG_LOG, "Removed geofence ${entry.key}")
                               },
                               onFailure = {
                                   debug(
                                           TAG_LOG, "Failed to remove geofence ${entry.key}")
                               }
                )
            }
            apply()
        }
    }

    private fun removeGeofence(id: String, onSuccess: () -> Unit, onFailure: (errorMessage: String?) -> Unit) =
        client.removeGeofences(listOf(id))
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it.message) }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest? =
        GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()

    private fun buildGeofence(id: String, lat: Double, lng: Double): Geofence? =
        Geofence.Builder()
                .setRequestId(id)
                .setLoiteringDelay(10)
                .setCircularRegion(lat, lng,
                                   DEFAULT_RADIUS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                .build()

    companion object {
        private const val TAG_LOG = "GeofencingClientLogger"
        private const val DEFAULT_RADIUS = 300F
    }
}