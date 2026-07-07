package com.adamwilkinson.standby

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.StandbyPagerScreen
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.theme.StandbyTheme

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
private fun StandbyRoot() {
    // Weather and Calendar join the list in M4/M5; face selection moves to
    // settings in M6.
    StandbyPagerScreen(
        pages = listOf(StandbyPage.Clock, StandbyPage.NowPlaying, StandbyPage.Battery),
        clockFace = ClockFaceStyle.Digital,
    )
}
