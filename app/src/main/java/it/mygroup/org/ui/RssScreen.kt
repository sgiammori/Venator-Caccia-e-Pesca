package it.mygroup.org.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.mygroup.org.network.RssCategory
import it.mygroup.org.network.RssItem
import it.mygroup.org.viewmodels.RssUiState
import it.mygroup.org.viewmodels.RssViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssScreen(
    rssViewModel: RssViewModel,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val uiState = rssViewModel.rssUiState
    val isRefreshing = rssViewModel.isRefreshing
    val selectedCategory = rssViewModel.selectedCategory

    Column(modifier = modifier.fillMaxSize()) {
        // Category Filter Row
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.ALL,
                        onClick = { rssViewModel.updateCategory(RssCategory.ALL) },
                        label = { Text("Tutte") },
                        leadingIcon = if (selectedCategory == RssCategory.ALL) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.CACCIA,
                        onClick = { rssViewModel.updateCategory(RssCategory.CACCIA) },
                        label = { Text("Caccia") },
                        leadingIcon = if (selectedCategory == RssCategory.CACCIA) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.PESCA,
                        onClick = { rssViewModel.updateCategory(RssCategory.PESCA) },
                        label = { Text("Pesca") },
                        leadingIcon = if (selectedCategory == RssCategory.PESCA) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null
                    )
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { rssViewModel.getRssFeeds(silent = true) },
            modifier = Modifier.weight(1f)
        ) {
            when (uiState) {
                is RssUiState.Loading -> LoadingScreen()
                is RssUiState.Success -> {
                    val filteredItems = remember(searchQuery, uiState.items, selectedCategory) {
                        uiState.items.filter { item ->
                            val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true)
                            val matchesCategory = selectedCategory == RssCategory.ALL || item.category == selectedCategory
                            matchesSearch && matchesCategory
                        }
                    }
                    RssList(items = filteredItems)
                }
                is RssUiState.Error -> ErrorScreen()
            }
        }
    }
}

@Composable
fun RssList(items: List<RssItem>, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            RssCard(
                item = item,
                onClick = { uriHandler.openUri(item.link) }
            )
        }
    }
}

@Composable
fun RssCard(item: RssItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val categoryColor = when(item.category) {
                            RssCategory.CACCIA -> Color(0xFF4CAF50)
                            RssCategory.PESCA -> Color(0xFF2196F3)
                            else -> Color.Gray
                        }
                        Surface(
                            color = categoryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.category.name,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = item.pubDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Errore nel caricamento del feed RSS")
    }
}
