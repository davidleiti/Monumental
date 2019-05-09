package ubb.thesis.david.monumental.geofencing

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.BaseApplication.Companion.GEOFENCE_CHANNEL_ID
import ubb.thesis.david.monumental.HostActivity
import ubb.thesis.david.monumental.R

class GeofenceTransitionsService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            debug(TAG_LOG, "A geofencing error has occurred: ${geofenceErrorString(event.errorCode)}")
        } else {
            handleTransitionEvent(event)
        }
    }

    private fun handleTransitionEvent(event: GeofencingEvent) {
        info(TAG_LOG, "GeofencingEvent triggered: $event")
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            sendNotification()
        }
    }

    private fun sendNotification() {
        val notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val actionIntent = Intent(this, HostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, actionIntent, 0)

        NotificationCompat.Builder(this, GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_logo_128)
                .setContentTitle(getString(R.string.title_notification))
                .setContentText(getString(R.string.content_notification))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
                .also { notification ->
                    notificationManager.notify(1234, notification)
                }
    }

    private fun geofenceErrorString(errorCode: Int) = when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> getString(R.string.geofencing_error_unavailable)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> getString(R.string.geofencing_error_limit_reached)
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> getString(R.string.geofencing_error_pi_limit_reached)
        else -> getString(R.string.geofencing_error_unknown)
    }

    companion object {
        private const val JOB_ID = 700
        private const val TAG_LOG = "GeofenceTransitionsServiceLogger"

        fun enqueueWork(context: Context, intent: Intent) =
            enqueueWork(context, GeofenceTransitionsService::class.java, JOB_ID, intent)
    }
}
