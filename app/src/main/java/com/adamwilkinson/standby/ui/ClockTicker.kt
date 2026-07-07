package com.adamwilkinson.standby.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Emits the current time aligned to second boundaries, so digit changes land
 * exactly on the tick instead of drifting through the second.
 */
@Composable
fun rememberCurrentTime(): State<LocalDateTime> =
    produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1_000 - System.currentTimeMillis() % 1_000)
        }
    }
