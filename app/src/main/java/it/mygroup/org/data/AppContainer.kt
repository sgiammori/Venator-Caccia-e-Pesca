package it.mygroup.org.data

import android.content.Context
import it.mygroup.org.network.CacciaPescaApi

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val cacciaPescaRepository: CacciaPescaRepository
    val rssRepository: RssRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineCacciaPescaRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [CacciaPescaRepository]
     */
    override val cacciaPescaRepository: CacciaPescaRepository by lazy {
        OfflineCacciaPescaRepository(CacciaPescaDatabase.getDatabase(context).itemDao())
    }

    override val rssRepository: RssRepository by lazy {
        NetworkRssRepository()
    }
}
