package com.adamwilkinson.standby

import android.content.Context
import com.adamwilkinson.standby.data.battery.BatteryRepository
import com.adamwilkinson.standby.data.media.MediaSessionRepository
import com.adamwilkinson.standby.data.settings.SettingsRepository
import com.adamwilkinson.standby.data.weather.WeatherApi
import com.adamwilkinson.standby.data.weather.WeatherRepository
import okhttp3.OkHttpClient

/**
 * Manual dependency container. Repositories are created lazily and shared
 * for the lifetime of the process.
 */
class AppContainer(private val appContext: Context) {

    private val okHttpClient by lazy { OkHttpClient() }

    val settingsRepository by lazy { SettingsRepository(appContext) }
    val mediaSessionRepository by lazy { MediaSessionRepository(appContext) }
    val batteryRepository by lazy { BatteryRepository(appContext) }
    val weatherRepository by lazy {
        WeatherRepository(appContext, WeatherApi(okHttpClient), settingsRepository)
    }
}
