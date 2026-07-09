package com.adamwilkinson.standby.ui.split

/** Widgets that can occupy one half of the split view. Ids are persisted. */
enum class PaneWidget(val id: String, val label: String) {
    Clock("clock", "Clock"),
    Weather("weather", "Weather"),
    Calendar("calendar", "Calendar"),
    Battery("battery", "Battery"),
    NowPlaying("media", "Now Playing");

    companion object {
        val All = entries.toList()

        fun fromId(id: String?): PaneWidget = entries.firstOrNull { it.id == id } ?: Clock
    }
}
