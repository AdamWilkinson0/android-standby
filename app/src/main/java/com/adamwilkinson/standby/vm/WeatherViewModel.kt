package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adamwilkinson.standby.data.weather.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    val state = repository.state

    init {
        // A plugged-in desk app can just poll while alive; no WorkManager needed.
        viewModelScope.launch {
            repository.start()
            while (isActive) {
                delay(REFRESH_INTERVAL_MS)
                repository.refreshIfStale()
            }
        }
    }

    fun hasLocationPermission() = repository.hasLocationPermission()

    fun onLocationPermissionGranted() {
        viewModelScope.launch { repository.refresh() }
    }

    fun retry() {
        viewModelScope.launch { repository.refresh() }
    }

    fun setCity(query: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(repository.setCity(query) != null) }
    }
}
