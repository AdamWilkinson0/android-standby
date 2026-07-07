package com.adamwilkinson.standby.data.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

class WeatherApi(private val client: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun forecast(latitude: Double, longitude: Double, fahrenheit: Boolean): ForecastResponse =
        get(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,is_day,wind_speed_10m" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto&forecast_days=1" +
                if (fahrenheit) "&temperature_unit=fahrenheit" else "",
        )

    suspend fun geocode(query: String): GeoResult? =
        get<GeocodingResponse>(
            "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=${URLEncoder.encode(query, Charsets.UTF_8.name())}&count=1",
        ).results?.firstOrNull()

    private suspend inline fun <reified T> get(url: String): T = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            json.decodeFromString<T>(response.body!!.string())
        }
    }
}
