package it.mygroup.org.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.mygroup.org.CacciaPescaApplication
import it.mygroup.org.viewmodels.ItemDetailsViewModel
import it.mygroup.org.viewmodels.ItemEntryViewModel
import it.mygroup.org.viewmodels.RssViewModel
import it.mygroup.org.viewmodels.StoricoPageViewModel
import it.mygroup.org.viewmodels.StoricoViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(cacciaPescaApplication().container.cacciaPescaRepository)
        }

        // Initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                cacciaPescaApplication().container.cacciaPescaRepository
            )
        }

        // Initializer for StoricoViewModel
        initializer {
            StoricoViewModel(cacciaPescaApplication().container.cacciaPescaRepository)
        }

        // Initializer for StoricoPageViewModel
        initializer {
            StoricoPageViewModel(
                this.createSavedStateHandle(),
                cacciaPescaApplication().container.cacciaPescaRepository
            )
        }
        
        // Initializer for RssViewModel
        initializer {
            RssViewModel(cacciaPescaApplication().container.rssRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [CacciaPescaApplication].
 */
fun CreationExtras.cacciaPescaApplication() : CacciaPescaApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CacciaPescaApplication)
