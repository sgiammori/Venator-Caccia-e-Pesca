package it.mygroup.org.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.mygroup.org.CacciaPescaApplication
import it.mygroup.org.data.ActivityType
import it.mygroup.org.data.CacciaPescaRepository
import it.mygroup.org.data.Item
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ItemEntryViewModel(private val storicoRepository: CacciaPescaRepository): ViewModel() {
    var itemUIState by mutableStateOf(ItemUIState())
        private set

    val homeUiState: StateFlow<HomeUiState> =
        storicoRepository.getItemsByDayStream(
            SimpleDateFormat("d", Locale.getDefault()).format(Calendar.getInstance().time),
            SimpleDateFormat("M", Locale.getDefault()).format(Calendar.getInstance().time),
            SimpleDateFormat("y", Locale.getDefault()).format(Calendar.getInstance().time)
        ).map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CacciaPescaApplication)
                val storicoRepository = application.container.cacciaPescaRepository
                ItemEntryViewModel(storicoRepository = storicoRepository)
            }
        }
    }

    fun updateUIStateDate(itemDetails: ItemDetails){
        itemUIState =
            ItemUIState(
                itemDetails = itemDetails,
                isValid = validateInput(itemDetails)
            )
    }

    fun updateUIState(itemDetails: ItemDetails){
        // Normalizzazione: converte la virgola in punto per il campo peso
        val normalizedDetails = itemDetails.copy(
            weight = itemDetails.weight.replace(',', '.')
        )
        itemUIState =
            ItemUIState(
                itemDetails = normalizedDetails,
                isValid = validateInput(normalizedDetails)
            )
    }

    suspend fun saveItem(){
        if(validateInput()){
            storicoRepository.insertItem(itemUIState.itemDetails.toItem())
        }
    }

    private fun validateInput(uiState: ItemDetails = itemUIState.itemDetails): Boolean {
        val quantity = uiState.quantity.toIntOrNull()
        val weight = uiState.weight.toDoubleOrNull()
        return uiState.preyName.isNotBlank() &&
                quantity != null && quantity > 0 &&
                weight != null && weight >= 0.0
    }
}

data class HomeUiState(val itemList: List<Item> = listOf())

data class ItemUIState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isValid: Boolean = false
)

data class ItemDetails(
    val id: Int = 0,
    val preyName: String = "",
    val activityType: ActivityType = ActivityType.HUNTING,
    val quantity: String = "1",
    val weight: String = "0.0",
    val location: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val notes: String = ""
)

fun ItemDetails.toItem(): Item =
    Item(
        id = id,
        preyName = preyName,
        activityType = activityType,
        quantity = quantity.toIntOrNull() ?: 1,
        weight = weight.toDoubleOrNull() ?: 0.0,
        location = location,
        day = day,
        month = month,
        year = year,
        notes = notes
    )

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    preyName = preyName,
    activityType = activityType,
    quantity = quantity.toString(),
    weight = weight.toString(),
    location = location,
    day = day,
    month = month,
    year = year,
    notes = notes
)
