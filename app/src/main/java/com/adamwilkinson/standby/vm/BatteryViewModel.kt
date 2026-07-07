package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adamwilkinson.standby.data.battery.BatteryRepository
import com.adamwilkinson.standby.data.battery.BatteryStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BatteryViewModel(repository: BatteryRepository) : ViewModel() {

    val status: StateFlow<BatteryStatus?> = repository.status
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
