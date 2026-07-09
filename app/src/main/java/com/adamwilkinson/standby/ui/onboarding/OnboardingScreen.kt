package com.adamwilkinson.standby.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import kotlinx.coroutines.launch

/**
 * Three quick, entirely skippable steps: media access, location, calendar.
 * Every page keeps its own graceful fallback, so nothing here is mandatory.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Re-checked whenever the user returns from the system settings screen.
    var accessCheckTrigger by remember { mutableIntStateOf(0) }
    var hasNotificationAccess by remember { mutableStateOf(false) }
    LifecycleResumeEffect(accessCheckTrigger) {
        hasNotificationAccess = NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
        onPauseOrDispose { }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { scope.launch { pagerState.animateScrollToPage(2) } }
    val calendarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onDone() }

    fun next() {
        if (pagerState.currentPage == 2) {
            onDone()
        } else {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> OnboardingStep(
                    title = "See what's playing",
                    body = "Standby shows and controls media from Spotify, YouTube and " +
                        "more. That needs notification access — nothing is read or stored.",
                    actionLabel = if (hasNotificationAccess) "✓ Connected" else "Open settings",
                    actionEnabled = !hasNotificationAccess,
                    onAction = {
                        accessCheckTrigger++
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                        )
                    },
                    onSkip = ::next,
                    skipLabel = if (hasNotificationAccess) "Continue" else "Skip",
                )

                1 -> OnboardingStep(
                    title = "Weather at a glance",
                    body = "Allow approximate location for local weather, or set a city " +
                        "later in settings.",
                    actionLabel = "Allow location",
                    onAction = {
                        locationLauncher.launch(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    },
                    onSkip = ::next,
                )

                2 -> OnboardingStep(
                    title = "Your day at a glance",
                    body = "Show upcoming events from your calendar on the standby screen.",
                    actionLabel = "Allow calendar",
                    onAction = {
                        calendarLauncher.launch(android.Manifest.permission.READ_CALENDAR)
                    },
                    onSkip = ::next,
                    skipLabel = "Skip and finish",
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) StandbyDim else StandbyFaint,
                        ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingStep(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    onSkip: () -> Unit,
    skipLabel: String = "Skip",
    actionEnabled: Boolean = true,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = onAction, enabled = actionEnabled) {
                Text(
                    actionLabel,
                    color = if (actionEnabled) MaterialTheme.colorScheme.primary else StandbyDim,
                )
            }
            TextButton(onClick = onSkip) {
                Text(skipLabel, color = StandbyDim)
            }
        }
    }
}
