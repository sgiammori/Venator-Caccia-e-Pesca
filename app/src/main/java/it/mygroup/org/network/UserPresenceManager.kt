package it.mygroup.org.network

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class UserPresenceManager private constructor(private val context: Context) : DefaultLifecycleObserver {

    companion object {
        @Volatile
        private var INSTANCE: UserPresenceManager? = null

        fun getInstance(context: Context): UserPresenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPresenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val APP_SALT = "Venator_Secure_2026_Salt_v1"
    private var cachedUserId: String? = null
    
    // Scope dedicato che sopravvive alla chiusura dei componenti UI
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val userId: String
        get() {
            if (cachedUserId != null) return cachedUserId!!
            
            var id = sharedPrefs.getString("user_id_v2", null)
            if (id == null) {
                id = "ID${generateDeviceUniqueId()}"
                sharedPrefs.edit().putString("user_id_v2", id).commit()
                Log.d("UserPresence", "Nuovo ID V2 generato e salvato: $id")
            }
            cachedUserId = id
            return id
        }

    private fun generateDeviceUniqueId(): String {
        try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) 
                ?: UUID.randomUUID().toString()
            
            val inputToHash = androidId + APP_SALT
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(inputToHash.toByteArray())
            val charPool = ('A'..'Z') + ('0'..'9')
            
            return digest.take(10).map { byte ->
                val index = (byte.toInt() and 0xFF) % charPool.size
                charPool[index]
            }.joinToString("")
            
        } catch (e: Exception) {
            return UUID.randomUUID().toString().take(10).uppercase()
        }
    }

    // --- Friendlist Management ---
    
    fun getFriends(): List<String> {
        val friendsSet = sharedPrefs.getStringSet("friend_list", emptySet())
        return friendsSet?.toList() ?: emptyList()
    }

    fun addFriend(friendId: String) {
        val friendsSet = sharedPrefs.getStringSet("friend_list", emptySet())?.toMutableSet() ?: mutableSetOf()
        friendsSet.add(friendId)
        sharedPrefs.edit().putStringSet("friend_list", friendsSet).apply()
    }

    fun removeFriend(friendId: String) {
        val friendsSet = sharedPrefs.getStringSet("friend_list", emptySet())?.toMutableSet() ?: mutableSetOf()
        friendsSet.remove(friendId)
        sharedPrefs.edit().putStringSet("friend_list", friendsSet).apply()
    }

    // --- Presence Management ---

    override fun onStart(owner: LifecycleOwner) {
        Log.d("UserPresence", "App ON START: setting active=true")
        updateStatus(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("UserPresence", "App ON STOP: setting active=false")
        updateStatus(false)
    }

    /**
     * Funzione speciale chiamata quando l'app viene chiusa forzatamente (swipe away).
     * Tenta di inviare lo stato offline in modo rapido.
     */
    fun forceOfflineSync() {
        updateStatus(false)
    }

    private fun updateStatus(isAttivo: Boolean) {
        val currentId = userId
        managerScope.launch {
            try {
                // NonCancellable assicura che il blocco termini anche se lo scope viene cancellato improvvisamente
                withContext(NonCancellable) {
                    // Usiamo un formato data ISO 8601 compatibile con MongoDB/Backend
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val currentDate = sdf.format(Date())
                    
                    // Inviamo 'attivo' come BOOLEAN (senza virgolette) per matchare il tipo nel DB
                    val json = """{
                        "userId": "$currentId", 
                        "attivo": $isAttivo,
                        "lastaccess": "$currentDate"
                    }"""
                    
                    Log.d("UserPresence", "Syncing status for $currentId: $json")
                    val response = CacciaPescaApi.retrofitService.authenticateApp(json)
                    Log.d("UserPresence", "Sync result: $response")
                }
            } catch (e: Exception) {
                Log.e("UserPresence", "Error syncing status for $currentId", e)
            }
        }
    }
}
