package it.mygroup.org.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.mygroup.org.InventoryTopAppBar
import it.mygroup.org.R
import it.mygroup.org.data.ActivityType
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import it.mygroup.org.viewmodels.HomeUiState
import it.mygroup.org.viewmodels.ItemDetails
import it.mygroup.org.viewmodels.ItemEntryViewModel
import it.mygroup.org.viewmodels.ItemUIState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ItemNewEntryDestination : NavigationDestination {
    override val route = "item_new_entry"
    override val titleRes = R.string.item_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    viewModel: ItemEntryViewModel = viewModel(factory = ItemEntryViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    NewEntryScreenContent(
        canNavigateBack = canNavigateBack,
        navigateBack = navigateBack,
        homeUiState = homeUiState,
        itemUiState = viewModel.itemUIState,
        onItemValueChange = viewModel::updateUIState,
        onSaveClick = {
            coroutineScope.launch {
                if (viewModel.itemUIState.itemDetails.day.isEmpty()) {
                    val calendar = Calendar.getInstance()
                    viewModel.updateUIStateDate(viewModel.itemUIState.itemDetails.copy(
                        day = SimpleDateFormat("d", Locale.getDefault()).format(calendar.time),
                        month = SimpleDateFormat("M", Locale.getDefault()).format(calendar.time),
                        year = SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
                    ))
                }
                viewModel.saveItem()
                navigateBack()
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreenContent(
    canNavigateBack: Boolean,
    navigateBack: () -> Unit,
    homeUiState: HomeUiState,
    itemUiState: ItemUIState,
    onItemValueChange: (ItemDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val uiSpec = rememberResponsiveUiSpec()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable { focusManager.clearFocus() },
        topBar = {
            InventoryTopAppBar(
                title = stringResource(ItemNewEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = { navigateBack() },
                scrollBehavior = scrollBehavior,
                showAdd = false,
                showStorico = false,
                showMenu = false
            )
        }
    ) { innerPadding ->
        NewEntryBody(
            itemDetails = itemUiState.itemDetails,
            itemUiState = itemUiState,
            onItemValueChange = onItemValueChange,
            onSaveClick = onSaveClick,
            uiSpec = uiSpec,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryBody(
    itemDetails: ItemDetails,
    itemUiState: ItemUIState,
    onSaveClick: () -> Unit,
    onItemValueChange: (ItemDetails) -> Unit,
    uiSpec: it.mygroup.org.ui.theme.ResponsiveUiSpec = rememberResponsiveUiSpec(),
    modifier: Modifier = Modifier
) {
    val commonHeight = if (uiSpec.isLargeText) 60.dp else 56.dp
    val sectionGap = if (uiSpec.isLargeText) 18.dp else 16.dp
    val blockGap = if (uiSpec.isLargeText) 26.dp else 24.dp
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = uiSpec.screenHorizontalPadding, vertical = if (uiSpec.isLargeText) 18.dp else 16.dp)
    ) {
        Text(
            text = "Nome preda",
            style = MaterialTheme.typography.labelLarge
        )
        TextField(
            modifier = Modifier.fillMaxWidth().heightIn(min = commonHeight),
            value = itemDetails.preyName,
            onValueChange = { onItemValueChange(itemDetails.copy(preyName = it)) },
            singleLine = true,
            label = { Text(text = "Inserisci nome") },
            colors = TextFieldDefaults.colors(),
        )
        
        Spacer(modifier = Modifier.height(sectionGap))

        Text(
            text = "Tipo di attività",
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
        ) {
            ActivityTypeButton(
                text = "Caccia",
                isSelected = itemDetails.activityType == ActivityType.HUNTING,
                onClick = { onItemValueChange(itemDetails.copy(activityType = ActivityType.HUNTING)) },
                modifier = Modifier.weight(1f)
            )
            ActivityTypeButton(
                text = "Pesca",
                isSelected = itemDetails.activityType == ActivityType.FISHING,
                onClick = { onItemValueChange(itemDetails.copy(activityType = ActivityType.FISHING)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(sectionGap))

        Text(
            text = "Quantità",
            style = MaterialTheme.typography.labelLarge
        )
        TextField(
            modifier = Modifier.fillMaxWidth().heightIn(min = commonHeight),
            value = itemDetails.quantity,
            onValueChange = { onItemValueChange(itemDetails.copy(quantity = it)) },
            singleLine = true,
            label = { Text(text = "Inserisci quantità") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(),
        )

        Spacer(modifier = Modifier.height(sectionGap))

        Text(
            text = "Peso (Kg)",
            style = MaterialTheme.typography.labelLarge
        )
        TextField(
            modifier = Modifier.fillMaxWidth().heightIn(min = commonHeight),
            value = itemDetails.weight,
            onValueChange = { onItemValueChange(itemDetails.copy(weight = it)) },
            singleLine = true,
            label = { Text(text = "Inserisci peso") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(),
        )

        Spacer(modifier = Modifier.height(blockGap))

        val displayDate = if (itemDetails.day.isNotEmpty()) {
            "${itemDetails.day}/${itemDetails.month}/${itemDetails.year}"
        } else {
            SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        }
        
        Text(
            text = "Data cattura",
            style = MaterialTheme.typography.labelLarge
        )
        Card(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (uiSpec.isLargeText) 26.dp else 24.dp)
                )
                Spacer(Modifier.width(if (uiSpec.isLargeText) 18.dp else 16.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(blockGap))

        Button(
            onClick = onSaveClick,
            enabled = itemUiState.isValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().heightIn(min = commonHeight)
        ) {
            Text(text = "Salva")
        }
        
        Spacer(modifier = Modifier.height(sectionGap))
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val initialDate = if (itemDetails.day.isNotEmpty()) {
            try {
                calendar.set(itemDetails.year.toInt(), itemDetails.month.toInt() - 1, itemDetails.day.toInt())
                calendar.timeInMillis
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        }
        
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        onItemValueChange(itemDetails.copy(
                            day = cal.get(Calendar.DAY_OF_MONTH).toString(),
                            month = (cal.get(Calendar.MONTH) + 1).toString(),
                            year = cal.get(Calendar.YEAR).toString()
                        ))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ActivityTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiSpec = rememberResponsiveUiSpec()

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = if (uiSpec.isLargeText) 44.dp else 40.dp),
        colors = if (isSelected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.filledTonalButtonColors()
        }
    ) {
        Text(text = text)
    }
}
