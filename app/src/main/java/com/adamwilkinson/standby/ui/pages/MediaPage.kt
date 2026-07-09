package com.adamwilkinson.standby.ui.pages

import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.adamwilkinson.standby.data.media.NowPlaying
import com.adamwilkinson.standby.ui.components.MusicNoteIcon
import com.adamwilkinson.standby.ui.components.PermissionCard
import com.adamwilkinson.standby.ui.components.PlayPauseIcon
import com.adamwilkinson.standby.ui.components.SkipIcon
import com.adamwilkinson.standby.ui.theme.ArtColors
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.ui.theme.rememberArtColors
import com.adamwilkinson.standby.vm.MediaViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MediaPage(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val context = LocalContext.current
    val hasAccess by viewModel.hasNotificationAccess.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            !hasAccess -> PermissionCard(
                title = "See what's playing",
                body = "Standby needs notification access to show and control " +
                    "media from Spotify, YouTube and other apps.",
                buttonLabel = "Open settings",
                onGrant = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    )
                },
            )

            nowPlaying == null -> NothingPlaying()

            else -> NowPlayingContent(
                media = nowPlaying!!,
                onPlayPause = viewModel::playPause,
                onSkipNext = viewModel::skipNext,
                onSkipPrevious = viewModel::skipPrevious,
            )
        }
    }
}

@Composable
private fun NothingPlaying() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MusicNoteIcon(color = StandbyFaint, modifier = Modifier.size(56.dp))
        Text(
            text = "Nothing playing",
            style = MaterialTheme.typography.bodyLarge,
            color = StandbyDim,
        )
    }
}

@Composable
private fun NowPlayingContent(
    media: NowPlaying,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
) {
    val artColors = rememberArtColors(media.art)
    val accent = artColors.accent

    // Rich colored base so sub-API-31 devices (where blur is a no-op) still
    // get a vibrant backdrop instead of pure black.
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to artColors.accentDark.copy(alpha = 0.8f),
                    1f to Color.Black,
                ),
            ),
    )
    // Saturation-boosted blurred artwork on top (blur needs API 31+).
    media.art?.let { art ->
        Image(
            bitmap = art.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(
                ColorMatrix().apply { setToSaturation(1.6f) },
            ),
            modifier = Modifier
                .fillMaxSize()
                .blur(90.dp)
                .alpha(0.45f),
        )
    }
    // Bottom scrim keeps text and controls legible over the color.
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.55f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.4f),
                ),
            ),
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        AlbumArt(
            art = media.art,
            artUri = media.artUri,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = accent,
                    spotColor = accent,
                )
                .clip(RoundedCornerShape(28.dp)),
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = media.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = StandbyDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(36.dp))
            TransportControls(
                media = media,
                accent = accent,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
            )
            Spacer(Modifier.height(32.dp))
            ProgressBar(media = media, colors = artColors)
        }
    }
}

@Composable
internal fun AlbumArt(art: Bitmap?, artUri: String?, modifier: Modifier = Modifier) {
    Crossfade(targetState = art, label = "albumArt", modifier = modifier) { bitmap ->
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            artUri != null -> AsyncImage(
                model = artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            else -> Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center,
            ) {
                MusicNoteIcon(color = StandbyFaint, modifier = Modifier.size(64.dp))
            }
        }
    }
}

@Composable
private fun TransportControls(
    media: NowPlaying,
    accent: Color,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(enabled = media.canSkipPrev, onClick = onSkipPrevious),
            contentAlignment = Alignment.Center,
        ) {
            SkipIcon(
                forward = false,
                color = if (media.canSkipPrev) MaterialTheme.colorScheme.onBackground else StandbyFaint,
                modifier = Modifier.size(26.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(accent)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            PlayPauseIcon(
                isPlaying = media.isPlaying,
                color = Color.Black,
                modifier = Modifier.size(32.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(enabled = media.canSkipNext, onClick = onSkipNext),
            contentAlignment = Alignment.Center,
        ) {
            SkipIcon(
                forward = true,
                color = if (media.canSkipNext) MaterialTheme.colorScheme.onBackground else StandbyFaint,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun ProgressBar(media: NowPlaying, colors: ArtColors) {
    // PlaybackState.position is a snapshot; extrapolate while playing so the
    // bar moves between callbacks instead of freezing.
    var position by remember(media) { mutableLongStateOf(extrapolatedPosition(media)) }
    LaunchedEffect(media) {
        while (isActive) {
            position = extrapolatedPosition(media)
            delay(1_000)
        }
    }

    if (media.durationMs > 0) {
        val fraction = (position.toFloat() / media.durationMs).coerceIn(0f, 1f)
        Box(
            Modifier
                .fillMaxWidth(0.9f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.accentDark, colors.accent),
                        ),
                    ),
            )
        }
    }
}

internal fun extrapolatedPosition(media: NowPlaying): Long {
    if (!media.isPlaying || media.positionUpdateTimeMs == 0L) return media.positionMs
    val elapsed = SystemClock.elapsedRealtime() - media.positionUpdateTimeMs
    return media.positionMs + (elapsed * media.playbackSpeed).toLong()
}

