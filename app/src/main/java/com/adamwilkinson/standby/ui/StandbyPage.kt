package com.adamwilkinson.standby.ui

/** The swipeable pages of the standby screen. Ids are stable for persistence. */
enum class StandbyPage(val id: String, val label: String) {
    Clock("clock", "Clock"),
    NowPlaying("media", "Now Playing"),
    Weather("weather", "Weather"),
    Calendar("calendar", "Calendar"),
    Battery("battery", "Battery");

    companion object {
        val Default = listOf(Clock, NowPlaying, Weather, Calendar, Battery)

        fun fromIds(joined: String?): List<StandbyPage> {
            if (joined.isNullOrBlank()) return Default
            val pages = joined.split(',').mapNotNull { id ->
                entries.firstOrNull { it.id == id.trim() }
            }
            return pages.ifEmpty { Default }
        }
    }
}
