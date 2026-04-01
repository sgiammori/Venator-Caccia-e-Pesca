package it.mygroup.org.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.delay
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

    // Visible items for lazy loading / pagination
    private var allFilteredItems: List<RssItem> = emptyList()
    val visibleItems = mutableStateListOf<RssItem>()
    private var currentPage = 1
    private val pageSize = 10

    private val feedConfigs = listOf(
        "https://cacciaepesca.azurewebsites.net/api/generate-feed?url=https://www.cacciapassione.com/notizie/ultime/&extractionMode=basic" to RssCategory.CACCIA,
        "https://cacciaepesca.azurewebsites.net/api/generate-feed?url=https://www.pescaok.it/articoli/&extractionMode=basic" to RssCategory.PESCA
    )

    init {
        getRssFeeds()
    }

    fun updateCategory(category: RssCategory) {
        selectedCategory = category
        refreshVisibleItems("")
    }

    fun getRssFeeds(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) rssUiState = RssUiState.Loading
            else isRefreshing = true
            
            try {
                Log.d("RssViewModel", "Starting to fetch RSS feeds sequentially...")
                val allItems = mutableListOf<RssItem>()
                
                for ((url, category) in feedConfigs) {
                    try {
                        Log.d("RssViewModel", "Fetching $category from $url")
                        val items = rssRepository.getRssFeed(url).map { it.copy(category = category) }
                        Log.d("RssViewModel", "Successfully fetched ${items.size} items for $category")
                        allItems.addAll(items)
                    } catch (e: Exception) {
                        Log.e("RssViewModel", "Error fetching category $category", e)
                        // Non-blocking: continue with next category even if one fails
                    }
                    // Aggiunto un delay di 1 secondo tra le richieste per evitare sovraccarico sulla Function App
                    delay(1000)
                }
                
                Log.d("RssViewModel", "Total items loaded: ${allItems.size}")
                
                val sortedItems = allItems.sortedByDescending { parseDate(it.pubDate) }
                
                rssUiState = RssUiState.Success(sortedItems)
                refreshVisibleItems("")
            } catch (e: IOException) {
                Log.e("RssViewModel", "IOException in getRssFeeds", e)
                if (!silent) rssUiState = RssUiState.Error
            } catch (e: Exception) {
                Log.e("RssViewModel", "Unexpected Exception in getRssFeeds", e)
                if (!silent) rssUiState = RssUiState.Error
            } finally {
                isRefreshing = false
            }
        }
    }

    fun refreshVisibleItems(searchQuery: String) {
        val state = rssUiState
        if (state is RssUiState.Success) {
            allFilteredItems = state.items.filter { item ->
                val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == RssCategory.ALL || item.category == selectedCategory
                matchesSearch && matchesCategory
            }
            currentPage = 1
            visibleItems.clear()
            loadMoreItems()
        }
    }

    fun loadMoreItems() {
        val start = (currentPage - 1) * pageSize
        if (start < allFilteredItems.size) {
            val end = minOf(start + pageSize, allFilteredItems.size)
            visibleItems.addAll(allFilteredItems.subList(start, end))
            currentPage++
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
        return Date(0)
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
