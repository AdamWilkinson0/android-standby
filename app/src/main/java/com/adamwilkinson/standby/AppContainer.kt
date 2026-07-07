package com.adamwilkinson.standby

import android.content.Context
import com.adamwilkinson.standby.data.media.MediaSessionRepository

/**
 * Manual dependency container. Repositories are created lazily and shared
 * for the lifetime of the process.
 */
class AppContainer(private val appContext: Context) {

    val mediaSessionRepository by lazy { MediaSessionRepository(appContext) }
}
