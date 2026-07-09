package com.adamwilkinson.standby.ui.split.panes

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.media.NowPlaying
import com.adamwilkinson.standby.ui.components.MusicNoteIcon
import com.adamwilkinson.standby.ui.components.PlayPauseIcon
import com.adamwilkinson.standby.ui.components.SkipIcon
import com.adamwilkinson.standby.ui.pages.AlbumArt
import com.adamwilkinson.standby.ui.pages.extrapolatedPosition
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.ui.theme.rememberArtColors
import com.adamwilkinson.standby.vm.MediaViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Compact now-playing card for one half of the split view. */
@Composable
fun NowPlayingPane(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val hasAccess by viewModel.hasNotificationAccess.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val media = nowPlaying
        when {
            !hasAccess -> PanePlaceholder(
                title = "See what's playing",
                hint = "Allow notification access on the Now Playing page",
                icon = { MusicNoteIcon(color = StandbyFaint, modifier = Modifier.size(36.dp)) },
            )

            media == null -> PanePlaceholder(
                title = "Nothing playing",
                icon = { MusicNoteIcon(color = StandbyFaint, modifier = Modifier.size(36.dp)) },
            )

            else -> PaneNowPlaying(
                media = media,
                onPlayPause = viewModel::playPause,
                onSkipNext = viewModel::skipNext,
                onSkipPrevious = viewModel::skipPrevious,
            )
        }
    }
}

@Composable
private fun PaneNowPlaying(
    media: NowPlaying,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
) {
    val artColors = rememberArtColors(media.art)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AlbumArt(
            art = media.art,
            artUri = media.artUri,
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp)),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = media.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = media.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = artColors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(enabled = media.canSkipPrev, onClick = onSkipPrevious),
                contentAlignment = Alignment.Center,
            ) {
                SkipIcon(
                    forward = false,
                    color = if (media.canSkipPrev) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        StandbyFaint
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(artColors.accent)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                PlayPauseIcon(
                    isPlaying = media.isPlaying,
                    color = Color.Black,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(enabled = media.canSkipNext, onClick = onSkipNext),
                contentAlignment = Alignment.Center,
            ) {
                SkipIcon(
                    forward = true,
                    color = if (media.canSkipNext) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        StandbyFaint
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        PaneProgressBar(media = media, accent = artColors.accent)
    }
}

@Composable
private fun PaneProgressBar(media: NowPlaying, accent: Color) {
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
                .fillMaxWidth(0.8f)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(3.dp)
                    .background(accent),
            )
        }
    }
}
