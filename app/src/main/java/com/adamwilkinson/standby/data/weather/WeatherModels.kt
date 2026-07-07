package com.adamwilkinson.standby.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Open-Meteo DTOs ---

@Serializable
data class ForecastResponse(
    val current: CurrentDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
)

@Serializable
data class DailyDto(
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>,
)

@Serializable
data class GeocodingResponse(val results: List<GeoResult>? = null)

@Serializable
data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
)

// --- Domain model (serializable so the last result can be cached) ---

@Serializable
data class Weather(
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val windKmh: Double,
    val todayHigh: Double,
    val todayLow: Double,
    val placeName: String?,
    val fahrenheit: Boolean,
    val fetchedAtMillis: Long,
)

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    /** No location permission and no manual city configured. */
    data object NeedsLocation : WeatherUiState

    data class Data(val weather: Weather, val stale: Boolean) : WeatherUiState

    /** Fetch failed; show the last known weather if we have one. */
    data class Error(val lastKnown: Weather?) : WeatherUiState
}

/** Visual grouping of WMO weather codes for icon selection. */
enum class WeatherKind {
    ClearDay, ClearNight, PartlyCloudy, Cloudy, Fog, Rain, Snow, Thunder,
}

fun weatherKind(code: Int, isDay: Boolean): WeatherKind = when (code) {
    0 -> if (isDay) WeatherKind.ClearDay else WeatherKind.ClearNight
    1, 2 -> if (isDay) WeatherKind.PartlyCloudy else WeatherKind.ClearNight
    3 -> WeatherKind.Cloudy
    45, 48 -> WeatherKind.Fog
    in 51..67, in 80..82 -> WeatherKind.Rain
    in 71..77, 85, 86 -> WeatherKind.Snow
    in 95..99 -> WeatherKind.Thunder
    else -> WeatherKind.Cloudy
}

fun weatherDescription(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mostly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61 -> "Light rain"
    63 -> "Rain"
    65 -> "Heavy rain"
    66, 67 -> "Freezing rain"
    71 -> "Light snow"
    73 -> "Snow"
    75 -> "Heavy snow"
    77 -> "Snow grains"
    80, 81 -> "Showers"
    82 -> "Heavy showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "—"
}
