package it.mygroup.org.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.mygroup.org.CacciaPescaApplication
import it.mygroup.org.data.RssRepository
import it.mygroup.org.network.RssCategory
import it.mygroup.org.network.RssItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface RssUiState {
    data class Success(val items: List<RssItem>) : RssUiState
    object Error : RssUiState
    object Loading : RssUiState
}

class RssViewModel(private val rssRepository: RssRepository) : ViewModel() {

    var rssUiState: RssUiState by mutableStateOf(RssUiState.Loading)
        private set
    
    var isRefreshing by mutableStateOf(false)
        private set

    var selectedCategory by mutableStateOf(RssCategory.ALL)
        private set

    private val feedConfigs = listOf(
        "https://cacciaepesca.azurewebsites.net/api/generate-feed?url=https://www.cacciapassione.com/notizie/ultime/&extractionMode=basic" to RssCategory.CACCIA,
        "https://cacciaepesca.azurewebsites.net/api/generate-feed?url=https://www.pescaok.it/articoli/&extractionMode=basic" to RssCategory.PESCA
    )

    init {
        getRssFeeds()
    }

    fun updateCategory(category: RssCategory) {
        selectedCategory = category
    }

    fun getRssFeeds(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) rssUiState = RssUiState.Loading
            else isRefreshing = true
            
            try {
                // Fetch feeds in parallel and assign categories
                val deferredItems = feedConfigs.map { (url, category) ->
                    async { 
                        try {
                            rssRepository.getRssFeed(url).map { it.copy(category = category) }
                        } catch (e: Exception) {
                            emptyList<RssItem>()
                        }
                    }
                }
                
                val allItems = deferredItems.flatMap { it.await() }
                
                // Sort by date descendant
                val sortedItems = allItems.sortedByDescending { parseDate(it.pubDate) }
                
                rssUiState = RssUiState.Success(sortedItems)
            } catch (e: IOException) {
                if (!silent) rssUiState = RssUiState.Error
            } catch (e: Exception) {
                if (!silent) rssUiState = RssUiState.Error
            } finally {
                isRefreshing = false
            }
        }
    }

    private fun parseDate(dateString: String): Date {
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.US).parse(dateString) ?: Date(0)
            } catch (e: Exception) {
                continue
            }
        }
        return Date(0) // Fallback for unknown formats
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CacciaPescaApplication)
                val rssRepository = application.container.rssRepository
                RssViewModel(rssRepository = rssRepository)
            }
        }
    }
}
