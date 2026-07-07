package com.adamwilkinson.standby

import android.content.Context

/**
 * Manual dependency container. Repositories are created lazily and shared
 * for the lifetime of the process.
 */
class AppContainer(private val appContext: Context)
