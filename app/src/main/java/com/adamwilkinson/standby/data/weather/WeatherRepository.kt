package com.adamwilkinson.standby.data.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.adamwilkinson.standby.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

private val Context.weatherCacheStore by preferencesDataStore(name = "weather_cache")
private val CACHED_WEATHER = stringPreferencesKey("cached_weather")

private const val STALE_AFTER_MS = 30 * 60 * 1000L

class WeatherRepository(
    private val context: Context,
    private val api: WeatherApi,
    private val settings: SettingsRepository,
) {

    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    /** Load the cached result (instant render) and refresh if it has gone stale. */
    suspend fun start() {
        if (_state.value is WeatherUiState.Loading) {
            loadCache()?.let { _state.value = WeatherUiState.Data(it, stale = true) }
        }
        refreshIfStale()
    }

    suspend fun refreshIfStale() {
        val current = _state.value
        val fresh = current is WeatherUiState.Data &&
            System.currentTimeMillis() - current.weather.fetchedAtMillis < STALE_AFTER_MS &&
            !current.stale
        if (!fresh) refresh()
    }

    suspend fun refresh() {
        val prefs = settings.current()
        val manual = prefs.manualCityName != null &&
            prefs.manualLatitude != null && prefs.manualLongitude != null

        val (latitude, longitude) = when {
            manual -> prefs.manualLatitude!! to prefs.manualLongitude!!
            else -> deviceCoordinates() ?: run {
                _state.value = lastKnown()?.let { WeatherUiState.Data(it, stale = true) }
                    ?: WeatherUiState.NeedsLocation
                return
            }
        }

        try {
            val response = api.forecast(latitude, longitude, prefs.useFahrenheit)
            val weather = Weather(
                temperature = response.current.temperature,
                weatherCode = response.current.weatherCode,
                isDay = response.current.isDay == 1,
                windKmh = response.current.windSpeed,
                todayHigh = response.daily.tempMax.firstOrNull() ?: response.current.temperature,
                todayLow = response.daily.tempMin.firstOrNull() ?: response.current.temperature,
                placeName = prefs.manualCityName,
                fahrenheit = prefs.useFahrenheit,
                fetchedAtMillis = System.currentTimeMillis(),
            )
            _state.value = WeatherUiState.Data(weather, stale = false)
            context.weatherCacheStore.edit {
                it[CACHED_WEATHER] = json.encodeToString(Weather.serializer(), weather)
            }
        } catch (e: Exception) {
            _state.value = WeatherUiState.Error(lastKnown())
        }
    }

    /** Geocodes the query and stores it as the manual city. Returns the match, or null. */
    suspend fun setCity(query: String): GeoResult? {
        val result = try {
            api.geocode(query.trim())
        } catch (e: Exception) {
            null
        } ?: return null
        settings.setManualCity(result.name, result.latitude, result.longitude)
        refresh()
        return result
    }

    private fun lastKnown(): Weather? =
        (_state.value as? WeatherUiState.Data)?.weather
            ?: (_state.value as? WeatherUiState.Error)?.lastKnown

    private suspend fun loadCache(): Weather? =
        context.weatherCacheStore.data.first()[CACHED_WEATHER]?.let {
            runCatching { json.decodeFromString(Weather.serializer(), it) }.getOrNull()
        }

    fun hasLocationPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private suspend fun deviceCoordinates(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val provider = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        ).firstOrNull(manager::isProviderEnabled) ?: return null

        val location: Location? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            withTimeoutOrNull(10_000) {
                suspendCancellableCoroutine { continuation ->
                    manager.getCurrentLocation(provider, null, context.mainExecutor) {
                        continuation.resume(it)
                    }
                }
            } ?: manager.getLastKnownLocation(provider)
        } else {
            manager.getLastKnownLocation(provider)
                ?: manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        }
        // Weather does not need precision; round to ~1km to avoid storing a fix.
        return location?.let {
            Math.round(it.latitude * 100.0) / 100.0 to Math.round(it.longitude * 100.0) / 100.0
        }
    }
}
