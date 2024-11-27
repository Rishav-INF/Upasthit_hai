package com.example.upasthithai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
                return
            }
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        Log.d("GeofenceReceiver", "Received geofence transition code: $geofenceTransition")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceReceiver", "Geofence Entered: ${geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId}")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "Geofence Exited: ${geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId}")
            }
            else -> Log.e("GeofenceReceiver", "Unknown geofence transition")
        }
    }
}
