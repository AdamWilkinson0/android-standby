package com.adamwilkinson.standby.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class PlugType { None, Ac, Usb, Wireless }

data class BatteryStatus(
    val percent: Int,
    val isCharging: Boolean,
    val isFull: Boolean,
    val plug: PlugType,
)

class BatteryRepository(private val context: Context) {

    /**
     * ACTION_BATTERY_CHANGED is sticky, so registration immediately returns
     * the current state — the flow emits instantly on collection.
     */
    val status: Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.toBatteryStatus()?.let(::trySend)
            }
        }
        val sticky = context.registerReceiver(
            receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        sticky?.toBatteryStatus()?.let(::trySend)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}

private fun Intent.toBatteryStatus(): BatteryStatus? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return null

    val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    return BatteryStatus(
        percent = (level * 100) / scale,
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING,
        isFull = status == BatteryManager.BATTERY_STATUS_FULL,
        plug = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PlugType.Ac
            BatteryManager.BATTERY_PLUGGED_USB -> PlugType.Usb
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.Wireless
            else -> PlugType.None
        },
    )
}
