package it.mygroup.org.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.InventoryTopAppBar
import it.mygroup.org.R
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.StoricoPageViewModel
import java.util.Locale

object StoricoPageScreenDestination : NavigationDestination {
    override val route = "storico_page_screen"
    override val titleRes = R.string.item_storicopage_title
    
    const val itemMonthArg = "month"
    const val itemYearArg = "year"
    val routeWithArgs = "$route/{$itemYearArg}/{$itemMonthArg}"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StoricoPageScreen(
    navigateToItemUpdate: (Int) -> Unit,
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoricoPageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.storicoPageUiState.collectAsState()
    val uiSpec = rememberResponsiveUiSpec()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // RIMUOVE LO SPAZIO VUOTO IN FONDO
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(StoricoPageScreenDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = { navigateBack() },
                scrollBehavior = scrollBehavior,
                showMenu = false
            )
        }
    ) { innerPadding ->
        if (uiState.itemList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Storico vuoto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            val groupedItems = uiState.itemList.groupBy { it.preyName.uppercase(Locale.getDefault()) }
            val totalWeight = uiState.itemList.sumOf { it.weight }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = uiSpec.screenHorizontalPadding,
                    end = uiSpec.screenHorizontalPadding,
                    bottom = 0.dp // Forza a zero per evitare gap sopra il banner
                ),
                verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp)) {
                            Text(
                                text = "Peso Totale Periodo",
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
                groupedItems.forEach { (preyNameKey, items) ->
                    val displayNome = (items.firstOrNull()?.preyName ?: preyNameKey)
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    
                    val totalPerProductWeight = items.sumOf { it.weight }
                    val totalPerProductQuantity = items.sumOf { it.quantity }
                    
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(if (uiSpec.isLargeText) 12.dp else 10.dp),
                            verticalArrangement = Arrangement.spacedBy(if (uiSpec.isLargeText) 6.dp else 4.dp)
                        ) {
                            Text(
                                text = "Totale $displayNome",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Peso: ${String.format(Locale.getDefault(), "%.2f", totalPerProductWeight)} Kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Quantità: $totalPerProductQuantity",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    items(items = items, key = { it.id }) { item ->
                        InventoryItem(
                            item = item,
                            modifier = Modifier
                                .clickable { navigateToItemUpdate(item.id) }
                        )
                    }
                }
            }
        }
    }
}

fun getMonthName(month: String): String {
    return when (month) {
        "1" -> "Gennaio"
        "2" -> "Febbraio"
        "3" -> "Marzo"
        "4" -> "Aprile"
        "5" -> "Maggio"
        "6" -> "Giugno"
        "7" -> "Luglio"
        "8" -> "Agosto"
        "9" -> "Settembre"
        "10" -> "Ottobre"
        "11" -> "Novembre"
        "12" -> "Dicembre"
        else -> ""
    }
}
