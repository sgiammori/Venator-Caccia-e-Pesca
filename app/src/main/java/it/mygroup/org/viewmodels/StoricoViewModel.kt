package it.mygroup.org.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.mygroup.org.data.CacciaPescaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StoricoViewModel(private val repository: CacciaPescaRepository) : ViewModel() {
    val storicoUiState: StateFlow<StoricoUiState> =
        repository.getYearsStream()
            .map { StoricoUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = StoricoUiState()
            )
}

data class StoricoUiState(val itemList: List<String> = listOf())
