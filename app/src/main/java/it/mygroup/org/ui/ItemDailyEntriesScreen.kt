package it.mygroup.org.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.R
import it.mygroup.org.data.Item
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.WidthClass
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.ItemEntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ItemDailyEntriesDestination : NavigationDestination {
    override val route = "item_daily_entries"
    override val titleRes = R.string.app_name
}

@Composable
fun ItemDailyEntries(
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val uiSpec = rememberResponsiveUiSpec()
    val fabPadding = if (uiSpec.isLargeText) uiSpec.screenHorizontalPadding + 4.dp else uiSpec.screenHorizontalPadding

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry,
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(fabPadding)
                    .size(if (uiSpec.isLargeText) 60.dp else 56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.item_entry_title)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        HomeBody(
            itemList = homeUiState.itemList,
            onItemClick = navigateToItemUpdate,
            modifier = Modifier.padding(innerPadding),
            uiSpec = uiSpec
        )
    }
}

@Composable
fun HomeBody(
    itemList: List<Item>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    val watermarkSize = when (uiSpec.widthClass) {
        WidthClass.Compact -> if (uiSpec.isLargeText) 72.sp else 80.sp
        WidthClass.Medium -> if (uiSpec.isLargeText) 84.sp else 92.sp
        WidthClass.Expanded -> if (uiSpec.isLargeText) 92.sp else 100.sp
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Testo di sfondo "Carniere" centrato
        Text(
            text = "Carniere",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = watermarkSize
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )

        Column(modifier = Modifier.fillMaxSize()) {
            if (itemList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_item_description),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(uiSpec.screenHorizontalPadding)
                            .offset(y = if (uiSpec.isLargeText) (-48).dp else (-60).dp)
                    )
                }
            } else {
                InventoryList(
                    itemList = itemList,
                    onItemClick = { onItemClick(it.id) },
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                    uiSpec = uiSpec
                )
            }
        }
    }
}

@Composable
private fun InventoryList(
    itemList: List<Item>,
    onItemClick: (Item) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            bottom = 0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
    ) {
        item {
            val calendar = Calendar.getInstance().time
            Text(
                text = stringResource(
                    R.string.date_stocked,
                    SimpleDateFormat("d", Locale.getDefault()).format(calendar),
                    SimpleDateFormat("M", Locale.getDefault()).format(calendar),
                    SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar)
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = uiSpec.screenHorizontalPadding,
                    vertical = if (uiSpec.isLargeText) 10.dp else 8.dp
                )
            )
        }

        item {
            val totalWeight = itemList.sumOf { it.weight }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = uiSpec.screenHorizontalPadding)
                    .padding(bottom = uiSpec.listItemSpacing),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp)) {
                    Text(
                        text = "Peso Totale Oggi",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f Kg", totalWeight),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items(items = itemList, key = { it.id }) { item ->
            InventoryItem(
                item = item,
                modifier = Modifier
                    .padding(horizontal = uiSpec.screenHorizontalPadding)
                    .clickable { onItemClick(item) }
            )
        }
    }
}

@Composable
fun InventoryItem(
    item: Item,
    modifier: Modifier = Modifier
) {
    val uiSpec = rememberResponsiveUiSpec()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (uiSpec.isLargeText) 3.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (uiSpec.isLargeText) 10.dp else 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.preyName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.day}/${item.month}/${item.year}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (uiSpec.isLargeText) 18.dp else 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.in_stock, item.quantity),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.weight_info, item.weight),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
