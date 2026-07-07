package com.adamwilkinson.standby

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.StandbyPagerScreen
import com.adamwilkinson.standby.ui.onboarding.OnboardingScreen
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.settings.SettingsScreen
import com.adamwilkinson.standby.ui.theme.StandbyTheme
import com.adamwilkinson.standby.vm.SettingsViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            StandbyTheme {
                StandbyEffects()
                StandbyRoot()
            }
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
    settingsViewModel: SettingsViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val current = settings
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

        showSettings -> SettingsScreen(onClose = { showSettings = false })

        else -> StandbyPagerScreen(
            pages = StandbyPage.fromIds(current.pageIds),
            clockFace = ClockFaceStyle.fromId(current.clockFaceId),
            onOpenSettings = { showSettings = true },
        )
    }
}
