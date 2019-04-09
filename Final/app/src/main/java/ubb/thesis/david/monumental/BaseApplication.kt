package ubb.thesis.david.monumental

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.JobIntentService

class BaseApplication : Application() {

    init {
        Instance = this
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        setupGeofencingChannel()
    }

    private fun setupGeofencingChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.name_channel_geofence)
            val descriptionText = getString(R.string.desc_channel_geofence)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(GEOFENCE_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(JobIntentService.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun getAppContext(): BaseApplication = Instance
        const val GEOFENCE_CHANNEL_ID = "GeofenceNotificationsChannel"

        private lateinit var Instance: BaseApplication
    }
}