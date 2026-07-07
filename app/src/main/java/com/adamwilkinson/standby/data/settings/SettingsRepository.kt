package com.adamwilkinson.standby.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class StandbySettings(
    val pageIds: String?,
    val clockFaceId: String?,
    val manualCityName: String?,
    val manualLatitude: Double?,
    val manualLongitude: Double?,
    val useFahrenheit: Boolean,
    val nightDimEnabled: Boolean,
    val onboardingComplete: Boolean,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PAGES = stringPreferencesKey("pages")
        val CLOCK_FACE = stringPreferencesKey("clock_face")
        val CITY_NAME = stringPreferencesKey("city_name")
        val CITY_LAT = doublePreferencesKey("city_lat")
        val CITY_LON = doublePreferencesKey("city_lon")
        val FAHRENHEIT = booleanPreferencesKey("fahrenheit")
        val NIGHT_DIM = booleanPreferencesKey("night_dim")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val settings: Flow<StandbySettings> = context.settingsDataStore.data.map { prefs ->
        StandbySettings(
            pageIds = prefs[Keys.PAGES],
            clockFaceId = prefs[Keys.CLOCK_FACE],
            manualCityName = prefs[Keys.CITY_NAME],
            manualLatitude = prefs[Keys.CITY_LAT],
            manualLongitude = prefs[Keys.CITY_LON],
            useFahrenheit = prefs[Keys.FAHRENHEIT] ?: false,
            nightDimEnabled = prefs[Keys.NIGHT_DIM] ?: true,
            onboardingComplete = prefs[Keys.ONBOARDING_DONE] ?: false,
        )
    }

    suspend fun current(): StandbySettings = settings.first()

    suspend fun setPageIds(ids: String) {
        context.settingsDataStore.edit { it[Keys.PAGES] = ids }
    }

    suspend fun setClockFace(id: String) {
        context.settingsDataStore.edit { it[Keys.CLOCK_FACE] = id }
    }

    suspend fun setManualCity(name: String, latitude: Double, longitude: Double) {
        context.settingsDataStore.edit {
            it[Keys.CITY_NAME] = name
            it[Keys.CITY_LAT] = latitude
            it[Keys.CITY_LON] = longitude
        }
    }

    suspend fun clearManualCity() {
        context.settingsDataStore.edit {
            it.remove(Keys.CITY_NAME)
            it.remove(Keys.CITY_LAT)
            it.remove(Keys.CITY_LON)
        }
    }

    suspend fun setUseFahrenheit(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.FAHRENHEIT] = value }
    }

    suspend fun setNightDimEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.NIGHT_DIM] = value }
    }

    suspend fun setOnboardingComplete() {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }
}
