package ubb.thesis.david.monumental.data.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ubb.thesis.david.monumental.utils.info

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        info("GeofenceBroadcastReceiverLogger", "onReceive() called, forwarding to service...")
        GeofenceTransitionsService.enqueueWork(
                context!!,
                intent!!
        )
    }
}