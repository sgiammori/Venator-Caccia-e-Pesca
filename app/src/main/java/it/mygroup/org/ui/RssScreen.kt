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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import it.mygroup.org.ui.theme.WidthClass
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.RssUiState
import it.mygroup.org.viewmodels.RssViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

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
    val uiSpec = rememberResponsiveUiSpec()

    val chipRowVerticalPadding = if (uiSpec.isLargeText) 10.dp else 8.dp
    val chipSpacing = if (uiSpec.isLargeText) 10.dp else 8.dp

    // Update visible items when search query changes
    LaunchedEffect(searchQuery) {
        rssViewModel.refreshVisibleItems(searchQuery)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Category Filter Row
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = chipRowVerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                contentPadding = PaddingValues(horizontal = uiSpec.screenHorizontalPadding)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.ALL,
                        onClick = { rssViewModel.updateCategory(RssCategory.ALL) },
                        label = { Text("Tutte", fontSize = uiSpec.chipTextSize) },
                        leadingIcon = if (selectedCategory == RssCategory.ALL) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        modifier = Modifier.height(uiSpec.chipHeight)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.CACCIA,
                        onClick = { rssViewModel.updateCategory(RssCategory.CACCIA) },
                        label = { Text("Caccia", fontSize = uiSpec.chipTextSize) },
                        leadingIcon = if (selectedCategory == RssCategory.CACCIA) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        modifier = Modifier.height(uiSpec.chipHeight)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == RssCategory.PESCA,
                        onClick = { rssViewModel.updateCategory(RssCategory.PESCA) },
                        label = { Text("Pesca", fontSize = uiSpec.chipTextSize) },
                        leadingIcon = if (selectedCategory == RssCategory.PESCA) {
                            { Icon(Icons.Default.Done, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        modifier = Modifier.height(uiSpec.chipHeight)
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
                    RssList(
                        items = rssViewModel.visibleItems,
                        onLoadMore = { rssViewModel.loadMoreItems() },
                        uiSpec = uiSpec
                    )
                }
                is RssUiState.Error -> ErrorScreen()
            }
        }
    }
}

@Composable
fun RssList(
    items: List<RssItem>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()

    // Rileva quando mancano 2 elementi alla fine per caricare i successivi
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = if (uiSpec.isLargeText) 10.dp else 8.dp,
            start = uiSpec.screenHorizontalPadding,
            end = uiSpec.screenHorizontalPadding,
            bottom = if (uiSpec.isLargeText) 18.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
    ) {
        items(items, key = { it.link + it.pubDate }) { item ->
            RssCard(
                item = item,
                onClick = { uriHandler.openUri(item.link) },
                uiSpec = uiSpec
            )
        }
    }
}

@Composable
fun RssCard(
    item: RssItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    val imageSize = when (uiSpec.widthClass) {
        WidthClass.Compact -> if (uiSpec.isLargeText) 88.dp else 80.dp
        WidthClass.Medium -> if (uiSpec.isLargeText) 96.dp else 88.dp
        WidthClass.Expanded -> if (uiSpec.isLargeText) 104.dp else 96.dp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (uiSpec.isLargeText) 3.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(if (uiSpec.isLargeText) 12.dp else 8.dp)
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
                            .size(imageSize)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(if (uiSpec.isLargeText) 10.dp else 8.dp))
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
                                modifier = Modifier.padding(
                                    horizontal = if (uiSpec.isLargeText) 6.dp else 4.dp,
                                    vertical = 2.dp
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                fontSize = if (uiSpec.isLargeText) 10.sp else 9.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = item.pubDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (uiSpec.isLargeText) 11.sp else 10.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(if (uiSpec.isLargeText) 6.dp else 4.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = if (uiSpec.isLargeText) 20.sp else 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(if (uiSpec.isLargeText) 6.dp else 4.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontSize = if (uiSpec.isLargeText) 13.sp else 12.sp,
                lineHeight = if (uiSpec.isLargeText) 18.sp else 16.sp
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
