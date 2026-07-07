package com.adamwilkinson.standby.data.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NowPlaying(
    val title: String,
    val artist: String,
    val album: String?,
    val art: Bitmap?,
    val artUri: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    /** SystemClock.elapsedRealtime() when positionMs was reported; used to extrapolate. */
    val positionUpdateTimeMs: Long,
    val playbackSpeed: Float,
    val canSkipNext: Boolean,
    val canSkipPrev: Boolean,
    val appPackage: String,
)

/**
 * Observes active media sessions system-wide (Spotify, YouTube, ...) and
 * exposes the "primary" one — the first actively playing session, falling
 * back to the most recent — as a StateFlow.
 *
 * Every MediaSessionManager call is gated on notification access AND wrapped
 * in try/catch: access can be revoked at any moment and getActiveSessions
 * throws SecurityException in that window.
 */
class MediaSessionRepository(private val context: Context) {

    private val sessionManager =
        context.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent =
        ComponentName(context, MediaNotificationListener::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _hasNotificationAccess = MutableStateFlow(checkAccess())
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()

    private var started = false
    private var activeController: MediaController? = null

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            chooseController(controllers.orEmpty().filterNotNull())
        }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() {
            attach(null)
            refreshSessions()
        }
    }

    private fun checkAccess(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

    /**
     * Re-checks access (call on every resume — the user may have just come
     * back from the notification-access settings screen) and starts or stops
     * session observation to match.
     */
    fun refreshAccessAndStart() {
        val granted = checkAccess()
        _hasNotificationAccess.value = granted
        if (granted && !started) {
            // After a fresh grant the system may not bind the listener until
            // reboot; rebinding explicitly makes access take effect now.
            NotificationListenerService.requestRebind(listenerComponent)
            start()
        } else if (granted) {
            refreshSessions()
        } else {
            stop()
        }
    }

    private fun start() {
        if (started) return
        try {
            sessionManager.addOnActiveSessionsChangedListener(
                sessionsListener, listenerComponent, mainHandler,
            )
            started = true
            refreshSessions()
        } catch (e: SecurityException) {
            started = false
            _hasNotificationAccess.value = false
        }
    }

    fun stop() {
        if (started) {
            sessionManager.removeOnActiveSessionsChangedListener(sessionsListener)
            started = false
        }
        attach(null)
    }

    private fun refreshSessions() {
        try {
            chooseController(sessionManager.getActiveSessions(listenerComponent))
        } catch (e: SecurityException) {
            _hasNotificationAccess.value = false
            stop()
        }
    }

    private fun chooseController(controllers: List<MediaController>) {
        val chosen = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()
        if (chosen?.sessionToken != activeController?.sessionToken) {
            attach(chosen)
        } else {
            publish()
        }
    }

    private fun attach(controller: MediaController?) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controller
        controller?.registerCallback(controllerCallback, mainHandler)
        publish()
    }

    private fun publish() {
        _nowPlaying.value = activeController?.toNowPlaying()
    }

    fun playPause() {
        val controller = activeController ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }
}

private fun MediaController.toNowPlaying(): NowPlaying? {
    val metadata = metadata ?: return null
    val state = playbackState
    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        ?: return null
    return NowPlaying(
        title = title,
        artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: "",
        album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM),
        // Apps are inconsistent: Spotify sends ALBUM_ART, YouTube often only ART,
        // some only send a URI. Try bitmaps first, keep the URI as a fallback.
        art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART),
        artUri = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ART_URI),
        isPlaying = state?.state == PlaybackState.STATE_PLAYING,
        positionMs = state?.position ?: 0L,
        durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
        positionUpdateTimeMs = state?.lastPositionUpdateTime ?: 0L,
        playbackSpeed = state?.playbackSpeed ?: 1f,
        canSkipNext = (state?.actions ?: 0L) and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
        canSkipPrev = (state?.actions ?: 0L) and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
        appPackage = packageName,
    )
}
