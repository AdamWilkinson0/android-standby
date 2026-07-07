package com.adamwilkinson.standby.vm

import androidx.lifecycle.ViewModel
import com.adamwilkinson.standby.data.media.MediaSessionRepository

class MediaViewModel(private val repository: MediaSessionRepository) : ViewModel() {

    val nowPlaying = repository.nowPlaying
    val hasNotificationAccess = repository.hasNotificationAccess

    fun onResumed() = repository.refreshAccessAndStart()

    fun playPause() = repository.playPause()
    fun skipNext() = repository.skipNext()
    fun skipPrevious() = repository.skipPrevious()
}
