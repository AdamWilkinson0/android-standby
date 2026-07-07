package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adamwilkinson.standby.data.settings.SettingsRepository
import com.adamwilkinson.standby.data.settings.StandbySettings
import com.adamwilkinson.standby.data.weather.WeatherRepository
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    val settings: StateFlow<StandbySettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setClockFace(style: ClockFaceStyle) {
        viewModelScope.launch { settingsRepository.setClockFace(style.id) }
    }

    fun setPageEnabled(page: StandbyPage, enabled: Boolean) {
        viewModelScope.launch {
            val current = StandbyPage.fromIds(settings.value?.pageIds)
            val updated = StandbyPage.Default.filter { candidate ->
                if (candidate == page) enabled else candidate in current
            }
            // Never allow zero pages; fall back to the clock.
            val ids = updated.ifEmpty { listOf(StandbyPage.Clock) }
                .joinToString(",") { it.id }
            settingsRepository.setPageIds(ids)
        }
    }

    fun setUseFahrenheit(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseFahrenheit(value)
            weatherRepository.refresh()
        }
    }

    fun setNightDimEnabled(value: Boolean) {
        viewModelScope.launch { settingsRepository.setNightDimEnabled(value) }
    }

    fun setCity(query: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(weatherRepository.setCity(query) != null) }
    }

    fun useDeviceLocation() {
        viewModelScope.launch {
            settingsRepository.clearManualCity()
            weatherRepository.refresh()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingComplete() }
    }

    fun setScreenBrightness(value: Float) {
        viewModelScope.launch { settingsRepository.setScreenBrightness(value) }
    }
}
