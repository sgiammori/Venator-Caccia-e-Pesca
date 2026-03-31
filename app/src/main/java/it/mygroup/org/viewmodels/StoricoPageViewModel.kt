package it.mygroup.org.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mygroup.org.data.CacciaPescaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StoricoPageViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CacciaPescaRepository
) : ViewModel() {
    private val year: String = checkNotNull(savedStateHandle["year"])
    private val month: String = checkNotNull(savedStateHandle["month"])

    val storicoPageUiState: StateFlow<HomeUiState> =
        (if (month == "0") {
            repository.getItemsByYearStream(year)
        } else {
            repository.getAllItemsOfMonthStream(month, year)
        }).map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = HomeUiState()
            )
}
