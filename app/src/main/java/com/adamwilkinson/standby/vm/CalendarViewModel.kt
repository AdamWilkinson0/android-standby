package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adamwilkinson.standby.data.calendar.CalendarEvent
import com.adamwilkinson.standby.data.calendar.CalendarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data object NoPermission : CalendarUiState
    data object Empty : CalendarUiState
    data class Events(val events: List<CalendarEvent>) : CalendarUiState
}

private const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    private val _state = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshNow()
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    private suspend fun refreshNow() {
        _state.value = when {
            !repository.hasPermission() -> CalendarUiState.NoPermission
            else -> repository.upcomingEvents()
                .takeIf { it.isNotEmpty() }
                ?.let { CalendarUiState.Events(it) }
                ?: CalendarUiState.Empty
        }
    }
}
