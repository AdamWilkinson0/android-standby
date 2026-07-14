package com.adamwilkinson.standby

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.settings.StandbySettings
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.StandbyPagerScreen
import com.adamwilkinson.standby.ui.onboarding.OnboardingScreen
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont
import com.adamwilkinson.standby.ui.settings.SettingsScreen
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.StandbyTheme
import com.adamwilkinson.standby.vm.SettingsViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            StandbyEffects()
            StandbyRoot(onApplyBrightness = ::applyBrightness)
        }
    }

    /** Overrides window brightness; null returns control to the system. */
    private fun applyBrightness(value: Float?) {
        window.attributes = window.attributes.apply {
            screenBrightness =
                value ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    /** Keep the screen awake and hide system bars while standby is showing. */
    @Composable
    private fun StandbyEffects() {
        DisposableEffect(Unit) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            onDispose {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

@Composable
private fun StandbyRoot(
    onApplyBrightness: (Float?) -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val current = settings
    LaunchedEffect(current?.screenBrightness) {
        onApplyBrightness(current?.screenBrightness)
    }

    StandbyTheme(accent = AccentPreset.fromId(current?.accentId)) {
        StandbyContent(
            current = current,
            showSettings = showSettings,
            onShowSettings = { showSettings = it },
            onApplyBrightness = onApplyBrightness,
            settingsViewModel = settingsViewModel,
        )
    }
}

@Composable
private fun StandbyContent(
    current: StandbySettings?,
    showSettings: Boolean,
    onShowSettings: (Boolean) -> Unit,
    onApplyBrightness: (Float?) -> Unit,
    settingsViewModel: SettingsViewModel,
) {
    when {
        // DataStore hasn't emitted yet; hold black rather than flashing UI.
        current == null -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )

        !current.onboardingComplete -> OnboardingScreen(
            onDone = settingsViewModel::completeOnboarding,
        )

        else -> Box(Modifier.fillMaxSize()) {
            StandbyPagerScreen(
                pages = StandbyPage.fromIds(current.pageIds),
                clockFace = ClockFaceStyle.fromId(current.clockFaceId),
                clockFont = ClockFont.fromId(current.clockFontId),
                nightDimEnabled = current.nightDimEnabled,
                brightness = current.screenBrightness ?: 1f,
                autoSplitMedia = current.autoSplitMedia,
                leftPaneId = current.leftPaneId,
                rightPaneId = current.rightPaneId,
                rightSplit = current.rightSplit,
                rightBottomPaneId = current.rightBottomPaneId,
                onLeftPaneChanged = settingsViewModel::setLeftPane,
                onRightPaneChanged = settingsViewModel::setRightPane,
                onRightBottomPaneChanged = settingsViewModel::setRightBottomPane,
                onRightSplitChanged = settingsViewModel::setRightSplit,
                onClockFaceSelected = settingsViewModel::setClockFace,
                onClockFontSelected = settingsViewModel::setClockFont,
                onAccentSelected = settingsViewModel::setAccent,
                onBrightnessChange = onApplyBrightness,
                onBrightnessCommit = settingsViewModel::setScreenBrightness,
                onOpenSettings = { onShowSettings(true) },
            )
            // Settings slide up over the pager, iOS sheet style.
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically(initialOffsetY = { it / 8 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 8 }) + fadeOut(),
            ) {
                SettingsScreen(onClose = { onShowSettings(false) })
            }
        }
    }
}
