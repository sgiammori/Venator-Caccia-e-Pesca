package it.mygroup.org

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import it.mygroup.org.network.PresenceService
import it.mygroup.org.network.UserPresenceManager
import it.mygroup.org.ui.theme.CacciatoriEPescatoriAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var presenceManager: UserPresenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
        
        // Inizializza il gestore della presenza utente
        presenceManager = UserPresenceManager.getInstance(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(presenceManager)

        // Avvia il servizio per monitorare la chiusura forzata dell'app (swipe away)
        startService(Intent(this, PresenceService::class.java))

        setContent {
            CacciatoriEPescatoriAppTheme {
                CacciaPescaApp()
            }
        }
    }
}
