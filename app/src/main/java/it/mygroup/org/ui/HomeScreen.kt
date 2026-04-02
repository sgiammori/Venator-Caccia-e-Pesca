package it.mygroup.org.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.InventoryTopAppBar
import it.mygroup.org.R
import it.mygroup.org.ui.components.SecondaryToolbar
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.viewmodels.RssViewModel

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_title
}

enum class HomeSubView {
    DEFAULT, RSS, MAP, DATABASE, AI_MANAGER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    onNavigateToStorico: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    isItemSaved: Boolean = false,
    onItemSavedConsumed: () -> Unit = {},
    rssViewModel: RssViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    
    var currentSubView by rememberSaveable { mutableStateOf(HomeSubView.RSS) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var aiInitialQuery by rememberSaveable { mutableStateOf("") }

    // Utilizzo pinnedScrollBehavior per mantenere l'app bar fissa
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(enabled = isSearchActive || currentSubView != HomeSubView.RSS) {
        if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        } else {
            currentSubView = HomeSubView.RSS
            searchQuery = ""
        }
    }

    LaunchedEffect(isItemSaved) {
        if (isItemSaved) {
            currentSubView = HomeSubView.DATABASE
            onItemSavedConsumed()
        }
    }

    LaunchedEffect(currentSubView) {
        if (currentSubView == HomeSubView.AI_MANAGER && isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        }
        // Se usciamo dall'AI Manager, resettiamo la query iniziale per la prossima volta
        if (currentSubView != HomeSubView.AI_MANAGER) {
            aiInitialQuery = ""
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                InventoryTopAppBar(
                    title = stringResource(HomeDestination.titleRes),
                    canNavigateBack = false,
                    navigateUp = {},
                    scrollBehavior = scrollBehavior,
                    showSearch = currentSubView == HomeSubView.RSS,
                    showAdd = true,
                    showStorico = true,
                    showMenu = true,
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onAddClick = navigateToItemEntry,
                    onNavigateToStorico = onNavigateToStorico,
                    onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                )
                SecondaryToolbar(
                    currentView = currentSubView,
                    onRssClick = { 
                        currentSubView = HomeSubView.RSS 
                        isSearchActive = false
                        searchQuery = "" 
                    },
                    onMapClick = { 
                        currentSubView = HomeSubView.MAP 
                        isSearchActive = false
                        searchQuery = ""
                    },
                    onDatabaseClick = { 
                        currentSubView = HomeSubView.DATABASE 
                        isSearchActive = false
                        searchQuery = ""
                    },
                    onAiManagerClick = { 
                        currentSubView = HomeSubView.AI_MANAGER 
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (currentSubView) {
                HomeSubView.DEFAULT -> ItemEntryScreen(
                    onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                    modifier = Modifier.fillMaxSize()
                )
                HomeSubView.RSS -> RssScreen(
                    rssViewModel = rssViewModel,
                    searchQuery = searchQuery,
                    modifier = Modifier.fillMaxSize()
                )
                HomeSubView.MAP -> MapsScreen(
                    onConsultAI = { query ->
                        aiInitialQuery = query
                        currentSubView = HomeSubView.AI_MANAGER
                    },
                    modifier = Modifier.fillMaxSize()
                )
                HomeSubView.DATABASE -> ItemDailyEntries(
                    navigateToItemEntry = navigateToItemEntry,
                    navigateToItemUpdate = navigateToItemUpdate,
                    modifier = Modifier.fillMaxSize()
                )
                HomeSubView.AI_MANAGER -> IAManagerScreen(
                    initialQuery = aiInitialQuery,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ItemEntryScreen(
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text("Venator - Benvenuto")
    }
}
