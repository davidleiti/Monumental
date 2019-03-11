package ubb.license.david.monumentalv0.services

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.ui.MainActivity
import ubb.license.david.monumentalv0.utils.debug

class GeofenceTransitionsService : IntentService("GeofenceTransitionsService") {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onHandleIntent(intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)

        if (event.hasError()) {
            debug(TAG_LOG, "A geofencing error has occurred: ${geofenceErrorString(event.errorCode)}")
        } else {
            handleTransitionEvent(event)
        }
    }

    private fun handleTransitionEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringFence = event.triggeringGeofences[0]
            sendNotification(triggeringFence.requestId)
        }
    }

    private fun sendNotification(landmarkId: String) {
        val notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val actionIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, actionIntent, 0)

        NotificationCompat.Builder(this, GEOFENCE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Enter transition")
            .setContentText("Entered geofence with id: $landmarkId")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            .also { notification ->
                notificationManager.notify(1234, notification)
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.name_channel_geofence)
            val descriptionText = getString(R.string.desc_channel_geofence)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(GEOFENCE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun geofenceErrorString(errorCode: Int) =
        when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                ERROR_NOT_AVAILABLE
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                ERROR_TOO_MANY_GEOFENCES
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                ERROR_TOO_MANY_PENDING_INTENTS
            else -> ERROR_UNKNOWN
        }

    companion object {
        private const val TAG_LOG = "GeofenceTransitionsService"
        const val GEOFENCE_CHANNEL_ID = "GeofenceNotificationsChannel"

        private const val ERROR_UNKNOWN = "Unknown error: Geofence service is not available!"
        private const val ERROR_NOT_AVAILABLE =
            "The service is not available, high accuracy location services not enabled!"
        private const val ERROR_TOO_MANY_GEOFENCES = "Geofence limit has been reached!"
        private const val ERROR_TOO_MANY_PENDING_INTENTS = "PendingIntent limit has been reached!"
    }
}
