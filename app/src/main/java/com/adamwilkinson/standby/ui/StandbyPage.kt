package com.adamwilkinson.standby.ui

/** The swipeable pages of the standby screen. Ids are stable for persistence. */
enum class StandbyPage(val id: String, val label: String) {
    Split("split", "Split View"),
    Clock("clock", "Clock"),
    NowPlaying("media", "Now Playing"),
    Weather("weather", "Weather"),
    Calendar("calendar", "Calendar"),
    Battery("battery", "Battery");

    companion object {
        val Default = listOf(Split, Clock, NowPlaying, Weather, Calendar, Battery)

        fun fromIds(joined: String?): List<StandbyPage> {
            if (joined.isNullOrBlank()) return Default
            val pages = joined.split(',').mapNotNull { id ->
                entries.firstOrNull { it.id == id.trim() }
            }
            if (pages.isEmpty()) return Default
            // Installs from before the split view existed keep their saved
            // page list and simply gain the split view up front.
            return if (Split in pages) pages else listOf(Split) + pages
        }
    }
}
