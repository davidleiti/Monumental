package ubb.thesis.david.monumental.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ubb.thesis.david.data.utils.info

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        info("GeofenceBroadcastReceiverLogger", "onReceive() called, forwarding to service...")
        GeofenceTransitionsService.enqueueWork(context!!, intent!!)
    }
}