package it.mygroup.org.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.InventoryTopAppBar
import it.mygroup.org.R
import it.mygroup.org.data.Item
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.ItemDetailsViewModel
import it.mygroup.org.viewmodels.ItemUIState
import it.mygroup.org.viewmodels.toItem
import kotlinx.coroutines.launch

object ItemDetailsDestination : NavigationDestination {
    override val route = "item_details"
    override val titleRes = R.string.item_detail_title
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    navigateToEditItem: (Int) -> Unit,
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val uiSpec = rememberResponsiveUiSpec()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(ItemDetailsDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = { navigateBack() },
                scrollBehavior = scrollBehavior,
                showAdd = false,
                showStorico = false,
                showMenu = false
            )
        },
        modifier = modifier
    ) { innerPadding ->
        ItemDetailsBody(
            itemDetailsUiState = uiState,
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteItem()
                    navigateBack()
                }
            },
            uiSpec = uiSpec,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun ItemDetailsBody(
    itemDetailsUiState: ItemUIState,
    onDelete: () -> Unit,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec(),
    modifier: Modifier = Modifier
) {
    val sectionGap = if (uiSpec.isLargeText) 18.dp else 16.dp

    Column(
        modifier = modifier.padding(horizontal = uiSpec.screenHorizontalPadding, vertical = sectionGap),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }

        ItemDetailsCard(
            item = itemDetailsUiState.itemDetails.toItem(),
            uiSpec = uiSpec,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
        
        Spacer(modifier = Modifier.height(sectionGap))

        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(horizontal = uiSpec.screenHorizontalPadding)
            )
        }
    }
}

@Composable
fun ItemDetailsCard(
    item: Item,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec(),
    modifier: Modifier = Modifier
) {
    val contentPadding = if (uiSpec.isLargeText) 18.dp else 16.dp

    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(if (uiSpec.isLargeText) 14.dp else 12.dp)
        ) {
            ItemDetailsRow(
                labelResID = R.string.item,
                itemDetail = item.preyName,
                modifier = Modifier.padding(
                    horizontal = if (uiSpec.isLargeText) 4.dp else 2.dp
                )
            )
            ItemDetailsRow(
                labelResID = R.string.quantity_in_stock,
                itemDetail = stringResource(R.string.quantity, item.quantity.toString()),
                modifier = Modifier.padding(
                    horizontal = if (uiSpec.isLargeText) 4.dp else 2.dp
                )
            )
            ItemDetailsRow(
                labelResID = R.string.weight_title,
                itemDetail = stringResource(R.string.weight_info, item.weight),
                modifier = Modifier.padding(
                    horizontal = if (uiSpec.isLargeText) 4.dp else 2.dp
                )
            )
            ItemDetailsRow(
                labelResID = R.string.data_saved,
                itemDetail = item.day + "/" + item.month + "/" + item.year,
                modifier = Modifier.padding(
                    horizontal = if (uiSpec.isLargeText) 4.dp else 2.dp
                )
            )
        }
    }
}

@Composable
private fun ItemDetailsRow(
    @StringRes labelResID: Int, itemDetail: String, modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(labelResID),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = itemDetail, 
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit, onDeleteCancel: () -> Unit, modifier: Modifier = Modifier
) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        })
}
