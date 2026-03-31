package it.mygroup.org.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mygroup.org.data.CacciaPescaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CacciaPescaRepository
) : ViewModel() {
    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])

    val uiState: StateFlow<ItemUIState> =
        repository.getItemStream(itemId)
            .filterNotNull()
            .map { it.toItemDetails() }
            .map { ItemUIState(itemDetails = it, isValid = true) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ItemUIState()
            )

    suspend fun deleteItem() {
        repository.deleteItem(uiState.value.itemDetails.toItem())
    }
}
