package it.mygroup.org

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.ui.navigation.CacciaPescaNavHost

@Composable
fun CacciaPescaApp(navController: NavHostController = rememberNavController()) {
    CacciaPescaNavHost(
        navController = navController
    )
}

/**
 * App bar to display title and conditionally display actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showSearch: Boolean = false,
    showAdd: Boolean = false,
    showStorico: Boolean = false,
    showMenu: Boolean = true,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    onNavigateToStorico: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    navigateUp: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val uiSpec = rememberResponsiveUiSpec()
    val actionSize = uiSpec.actionButtonSize

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "Cerca...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = uiSpec.inputTextSize)
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = uiSpec.inputTextSize),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                            onSearchActiveChange(false)
                            onSearchQueryChange("")
                        },
                            modifier = Modifier.size(actionSize)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Chiudi ricerca")
                        }
                    }
                )
            } else {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        modifier = modifier,
        windowInsets = WindowInsets(0, 0, 0, 0),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp, modifier = Modifier.size(actionSize)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                if (showSearch) {
                    IconButton(onClick = { onSearchActiveChange(true) }, modifier = Modifier.size(actionSize)) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
                if (showAdd) {
                    IconButton(onClick = onAddClick, modifier = Modifier.size(actionSize)) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                if (showStorico) {
                    IconButton(onClick = onNavigateToStorico, modifier = Modifier.size(actionSize)) {
                        Icon(Icons.Default.History, contentDescription = "Storico")
                    }
                }
                if (showMenu) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(actionSize)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.privacy_policy_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToPrivacyPolicy()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
