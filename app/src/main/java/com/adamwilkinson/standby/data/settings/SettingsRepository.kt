package com.adamwilkinson.standby.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class StandbySettings(
    val pageIds: String?,
    val clockFaceId: String?,
    val clockFontId: String?,
    val accentId: String?,
    val manualCityName: String?,
    val manualLatitude: Double?,
    val manualLongitude: Double?,
    val useFahrenheit: Boolean,
    val nightDimEnabled: Boolean,
    val autoSplitMedia: Boolean,
    val leftPaneId: String?,
    val rightPaneId: String?,
    /** When true the right half stacks two widgets (top = rightPaneId, bottom = rightBottomPaneId). */
    val rightSplit: Boolean,
    val rightBottomPaneId: String?,
    val onboardingComplete: Boolean,
    /** Window brightness override 0.05..1, or null to follow the system. */
    val screenBrightness: Float?,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PAGES = stringPreferencesKey("pages")
        val CLOCK_FACE = stringPreferencesKey("clock_face")
        val CLOCK_FONT = stringPreferencesKey("clock_font")
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
        val AUTO_SPLIT_MEDIA = booleanPreferencesKey("auto_split_media")
        val PANE_LEFT = stringPreferencesKey("pane_left")
        val PANE_RIGHT = stringPreferencesKey("pane_right")
        val PANE_RIGHT_SPLIT = booleanPreferencesKey("pane_right_split")
        val PANE_RIGHT_BOTTOM = stringPreferencesKey("pane_right_bottom")
        val CITY_NAME = stringPreferencesKey("city_name")
        val CITY_LAT = doublePreferencesKey("city_lat")
        val CITY_LON = doublePreferencesKey("city_lon")
        val FAHRENHEIT = booleanPreferencesKey("fahrenheit")
        val NIGHT_DIM = booleanPreferencesKey("night_dim")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val BRIGHTNESS = floatPreferencesKey("brightness")
    }

    val settings: Flow<StandbySettings> = context.settingsDataStore.data.map { prefs ->
        StandbySettings(
            pageIds = prefs[Keys.PAGES],
            clockFaceId = prefs[Keys.CLOCK_FACE],
            clockFontId = prefs[Keys.CLOCK_FONT],
            accentId = prefs[Keys.ACCENT_THEME],
            autoSplitMedia = prefs[Keys.AUTO_SPLIT_MEDIA] ?: true,
            leftPaneId = prefs[Keys.PANE_LEFT],
            rightPaneId = prefs[Keys.PANE_RIGHT],
            rightSplit = prefs[Keys.PANE_RIGHT_SPLIT] ?: false,
            rightBottomPaneId = prefs[Keys.PANE_RIGHT_BOTTOM],
            manualCityName = prefs[Keys.CITY_NAME],
            manualLatitude = prefs[Keys.CITY_LAT],
            manualLongitude = prefs[Keys.CITY_LON],
            useFahrenheit = prefs[Keys.FAHRENHEIT] ?: false,
            nightDimEnabled = prefs[Keys.NIGHT_DIM] ?: true,
            onboardingComplete = prefs[Keys.ONBOARDING_DONE] ?: false,
            screenBrightness = prefs[Keys.BRIGHTNESS],
        )
    }

    suspend fun current(): StandbySettings = settings.first()

    suspend fun setPageIds(ids: String) {
        context.settingsDataStore.edit { it[Keys.PAGES] = ids }
    }

    suspend fun setClockFace(id: String) {
        context.settingsDataStore.edit { it[Keys.CLOCK_FACE] = id }
    }

    suspend fun setClockFont(id: String) {
        context.settingsDataStore.edit { it[Keys.CLOCK_FONT] = id }
    }

    suspend fun setAccent(id: String) {
        context.settingsDataStore.edit { it[Keys.ACCENT_THEME] = id }
    }

    suspend fun setAutoSplitMedia(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SPLIT_MEDIA] = value }
    }

    suspend fun setLeftPane(id: String) {
        context.settingsDataStore.edit { it[Keys.PANE_LEFT] = id }
    }

    suspend fun setRightPane(id: String) {
        context.settingsDataStore.edit { it[Keys.PANE_RIGHT] = id }
    }

    suspend fun setRightSplit(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.PANE_RIGHT_SPLIT] = value }
    }

    suspend fun setRightBottomPane(id: String) {
        context.settingsDataStore.edit { it[Keys.PANE_RIGHT_BOTTOM] = id }
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

    suspend fun setScreenBrightness(value: Float) {
        context.settingsDataStore.edit { it[Keys.BRIGHTNESS] = value }
    }
}
