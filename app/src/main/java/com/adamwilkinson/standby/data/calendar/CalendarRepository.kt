package com.adamwilkinson.standby.data.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class CalendarEvent(
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val allDay: Boolean,
    val color: Int,
)

class CalendarRepository(private val context: Context) {

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Upcoming event instances in the next 48 hours. Queries Instances rather
     * than Events so recurring events expand correctly.
     */
    suspend fun upcomingEvents(limit: Int = 10): List<CalendarEvent> =
        withContext(Dispatchers.IO) {
            if (!hasPermission()) return@withContext emptyList()

            val begin = System.currentTimeMillis()
            val end = begin + TimeUnit.HOURS.toMillis(48)
            val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .also { ContentUris.appendId(it, begin) }
                .also { ContentUris.appendId(it, end) }
                .build()
            val projection = arrayOf(
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.DISPLAY_COLOR,
            )

            val events = mutableListOf<CalendarEvent>()
            context.contentResolver.query(
                uri, projection, null, null,
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                while (cursor.moveToNext() && events.size < limit) {
                    events += CalendarEvent(
                        title = cursor.getString(0)?.ifBlank { null } ?: "Untitled",
                        beginMillis = cursor.getLong(1),
                        endMillis = cursor.getLong(2),
                        allDay = cursor.getInt(3) == 1,
                        color = cursor.getInt(4),
                    )
                }
            }
            events
        }
}
