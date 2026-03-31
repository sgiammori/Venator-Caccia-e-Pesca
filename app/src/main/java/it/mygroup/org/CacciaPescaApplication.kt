package it.mygroup.org

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import it.mygroup.org.data.AppContainer
import it.mygroup.org.data.AppDataContainer

class CacciaPescaApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) { }

        // Automatically configure test devices only in Debug builds
        // Use the application's BuildConfig to check for DEBUG mode
        if (BuildConfig.DEBUG) {
            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("b785dc31fbad6922"))
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
    }
}
