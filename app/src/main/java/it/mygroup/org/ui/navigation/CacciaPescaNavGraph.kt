package it.mygroup.org.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import it.mygroup.org.ui.HomeDestination
import it.mygroup.org.ui.HomeScreen
import it.mygroup.org.ui.ItemDetailsDestination
import it.mygroup.org.ui.ItemDetailsScreen
import it.mygroup.org.ui.ItemNewEntryDestination
import it.mygroup.org.ui.NewEntryScreen
import it.mygroup.org.ui.StoricoPageScreen
import it.mygroup.org.ui.StoricoPageScreenDestination
import it.mygroup.org.ui.StoricoScreen
import it.mygroup.org.ui.StoricoScreenDestination
import it.mygroup.org.ui.components.AdBanner
import it.mygroup.org.ui.components.AdBannerController

@Composable
fun CacciaPescaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (AdBannerController.isVisible) {
                AdBanner()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = HomeDestination.route) {
                val isItemSaved = it.savedStateHandle.get<Boolean>("isItemSaved") ?: false
                HomeScreen(
                    navigateToItemEntry = { navController.navigate(ItemNewEntryDestination.route) },
                    navigateToItemUpdate = { itemId -> 
                        navController.navigate("${ItemDetailsDestination.route}/$itemId")
                    },
                    onNavigateToStorico = { navController.navigate(StoricoScreenDestination.route) },
                    onNavigateToPrivacyPolicy = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.iubenda.com/privacy-policy/48318932"))
                        context.startActivity(intent)
                    },
                    isItemSaved = isItemSaved,
                    onItemSavedConsumed = { it.savedStateHandle["isItemSaved"] = false }
                )
            }
            composable(route = ItemNewEntryDestination.route) {
                NewEntryScreen(
                    canNavigateBack = true,
                    navigateBack = { 
                        navController.previousBackStackEntry?.savedStateHandle?.set("isItemSaved", true)
                        navController.navigateUp() 
                    }
                )
            }
            composable(
                route = ItemDetailsDestination.routeWithArgs,
                arguments = listOf(navArgument(ItemDetailsDestination.itemIdArg) {
                    type = NavType.IntType
                })
            ) {
                ItemDetailsScreen(
                    navigateToEditItem = { /* TODO: navigate to edit item */ },
                    canNavigateBack = true,
                    navigateBack = { navController.navigateUp() }
                )
            }
            composable(route = StoricoScreenDestination.route) {
                StoricoScreen(
                    canNavigateBack = true,
                    navigateBack = { navController.navigateUp() },
                    onNavigateToStoricoPage = { year, month ->
                        navController.navigate("${StoricoPageScreenDestination.route}/$year/$month")
                    }
                )
            }
            composable(
                route = StoricoPageScreenDestination.routeWithArgs,
                arguments = listOf(
                    navArgument(StoricoPageScreenDestination.itemYearArg) { type = NavType.StringType },
                    navArgument(StoricoPageScreenDestination.itemMonthArg) { type = NavType.StringType }
                )
            ) {
                StoricoPageScreen(
                    canNavigateBack = true,
                    navigateBack = { navController.navigateUp() },
                    navigateToItemUpdate = { itemId ->
                        navController.navigate("${ItemDetailsDestination.route}/$itemId")
                    },
                )
            }
        }
    }
}
