package it.mygroup.org.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.InventoryTopAppBar
import it.mygroup.org.R
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.StoricoViewModel

object StoricoScreenDestination : NavigationDestination {
    override val route = "storico_screen"
    override val titleRes = R.string.item_storico_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoricoScreen(
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    onNavigateToStoricoPage: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoricoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val storicoUiState by viewModel.storicoUiState.collectAsState()
    val uiSpec = rememberResponsiveUiSpec()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(StoricoScreenDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = { navigateBack() },
                scrollBehavior = scrollBehavior,
                showAdd = false,
                showStorico = false,
                showMenu = false
            )
        }
    ) { innerPadding ->
        if (storicoUiState.itemList.isEmpty()) {
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
            StoricoList(
                itemList = storicoUiState.itemList,
                onItemClick = onNavigateToStoricoPage,
                contentPadding = innerPadding,
                modifier = Modifier.padding(horizontal = uiSpec.screenHorizontalPadding),
                uiSpec = uiSpec
            )
        }
    }
}

@Composable
private fun StoricoList(
    itemList: List<String>,
    onItemClick: (String, String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(uiSpec.listItemSpacing)
    ) {
        items(items = itemList, key = { it }) { item ->
            StoricoItem(item = item,
                modifier = Modifier
                    .clickable { onItemClick(item, "0") },
                year = item,
                onItemClick = onItemClick,
                uiSpec = uiSpec
            )
        }
    }
}

@Composable
private fun StoricoItem(
    item: String,
    modifier: Modifier = Modifier,
    year: String,
    onItemClick: (String, String) -> Unit,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = if (uiSpec.isLargeText) 3.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (uiSpec.isLargeText) 14.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            MinimalDropdownMenuByStorico(year = year, onItemClick = onItemClick, uiSpec = uiSpec)
        }
    }
}

@Composable
fun MinimalDropdownMenuByStorico(
    year: String,
    onItemClick: (String, String) -> Unit,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(if (uiSpec.isLargeText) 10.dp else 8.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(uiSpec.actionButtonSize)) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val months = listOf(
                "Gennaio" to "1", "Febbraio" to "2", "Marzo" to "3", "Aprile" to "4",
                "Maggio" to "5", "Giugno" to "6", "Luglio" to "7", "Agosto" to "8",
                "Settembre" to "9", "Ottobre" to "10", "Novembre" to "11", "Dicembre" to "12"
            )
            months.forEach { (name, index) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onItemClick(year, index)
                    }
                )
            }
        }
    }
}
