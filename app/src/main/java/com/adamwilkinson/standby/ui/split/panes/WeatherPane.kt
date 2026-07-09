package com.adamwilkinson.standby.ui.split.panes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.weather.WeatherUiState
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.pages.WeatherContent
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.vm.StandbyViewModels
import com.adamwilkinson.standby.vm.WeatherViewModel

@Composable
fun WeatherPane(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            WeatherUiState.Loading -> CircularProgressIndicator(color = StandbyFaint)

            WeatherUiState.NeedsLocation -> PanePlaceholder(
                title = "Weather needs a location",
                hint = "Set it up on the Weather page",
            )

            is WeatherUiState.Data -> WeatherContent(current.weather, WidgetSize.Pane)

            is WeatherUiState.Error ->
                if (current.lastKnown != null) {
                    WeatherContent(current.lastKnown, WidgetSize.Pane)
                } else {
                    PanePlaceholder(title = "Couldn't load weather")
                }
        }
    }
}
