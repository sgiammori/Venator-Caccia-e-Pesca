package it.mygroup.org.network

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class PresenceService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("PresenceService", "App swiped away! Sending active=false...")
        
        // We trigger the status update. Since the process is about to die, 
        // we use a blocking call or a very quick fire-and-forget.
        val presenceManager = UserPresenceManager.getInstance(this)
        presenceManager.forceOfflineSync()
        
        // Give it a tiny bit of time to initiate the request before the service stops
        Thread.sleep(300) 
        stopSelf()
    }
}
