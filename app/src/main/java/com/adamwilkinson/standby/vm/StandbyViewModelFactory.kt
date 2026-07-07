package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.adamwilkinson.standby.StandbyApplication

/** Single factory for all app ViewModels, backed by the AppContainer. */
object StandbyViewModels {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MediaViewModel(container().mediaSessionRepository)
        }
        initializer {
            BatteryViewModel(container().batteryRepository)
        }
        initializer {
            WeatherViewModel(container().weatherRepository)
        }
    }

    private fun androidx.lifecycle.viewmodel.CreationExtras.container() =
        (this[APPLICATION_KEY] as StandbyApplication).container
}
