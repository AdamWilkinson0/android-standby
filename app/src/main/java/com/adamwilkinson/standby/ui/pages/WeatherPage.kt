package com.adamwilkinson.standby.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.weather.Weather
import com.adamwilkinson.standby.data.weather.WeatherUiState
import com.adamwilkinson.standby.data.weather.weatherDescription
import com.adamwilkinson.standby.data.weather.weatherKind
import com.adamwilkinson.standby.ui.WidgetSize
import com.adamwilkinson.standby.ui.components.WeatherGlyph
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.LocalAccent
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import com.adamwilkinson.standby.vm.StandbyViewModels
import com.adamwilkinson.standby.vm.WeatherViewModel
import kotlin.math.roundToInt

@Composable
fun WeatherPage(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            WeatherUiState.Loading -> CircularProgressIndicator(color = StandbyFaint)

            WeatherUiState.NeedsLocation -> LocationSetup(viewModel)

            is WeatherUiState.Data -> WeatherContent(current.weather)

            is WeatherUiState.Error ->
                if (current.lastKnown != null) {
                    WeatherContent(current.lastKnown)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Couldn't load weather",
                            style = MaterialTheme.typography.bodyLarge,
                            color = StandbyDim,
                        )
                        OutlinedButton(onClick = viewModel::retry) { Text("Retry") }
                    }
                }
        }
    }
}

@Composable
private fun LocationSetup(viewModel: WeatherViewModel) {
    var city by rememberSaveable { mutableStateOf("") }
    var cityNotFound by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onLocationPermissionGranted()
    }

    Column(
        modifier = Modifier.widthIn(max = 440.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Where are you?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Allow approximate location, or type a city.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = { permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION) },
        ) {
            Text("Use my location")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    cityNotFound = false
                },
                placeholder = { Text("City name") },
                singleLine = true,
                isError = cityNotFound,
                modifier = Modifier.width(240.dp),
            )
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                enabled = city.isNotBlank(),
                onClick = {
                    viewModel.setCity(city) { found -> cityNotFound = !found }
                },
            ) {
                Text("Set")
            }
        }
        if (cityNotFound) {
            Text(
                text = "City not found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun WeatherContent(
    weather: Weather,
    size: WidgetSize = WidgetSize.Full,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccent.current
    val unit = if (weather.fahrenheit) "°F" else "°C"
    val tempStyle = MaterialTheme.typography.displayLarge.copy(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = if (size == WidgetSize.Full) 150.sp else 96.sp,
        fontFeatureSettings = TABULAR_NUMS,
    )
    val rangeLine = buildString {
        append("H ${weather.todayHigh.roundToInt()}$unit   ")
        append("L ${weather.todayLow.roundToInt()}$unit")
        if (size == WidgetSize.Full) weather.placeName?.let { append("   $it") }
    }

    if (size == WidgetSize.Full) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(56.dp),
        ) {
            WeatherGlyph(
                kind = weatherKind(weather.weatherCode, weather.isDay),
                modifier = Modifier.size(180.dp),
            )
            Column {
                Text(
                    text = "${weather.temperature.roundToInt()}°",
                    style = tempStyle,
                    color = accent.primary,
                )
                Text(
                    text = weatherDescription(weather.weatherCode),
                    style = MaterialTheme.typography.headlineMedium,
                    color = accent.secondary,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = rangeLine,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StandbyDim,
                )
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeatherGlyph(
                kind = weatherKind(weather.weatherCode, weather.isDay),
                modifier = Modifier.size(84.dp),
            )
            Text(
                text = "${weather.temperature.roundToInt()}°",
                style = tempStyle,
                color = accent.primary,
            )
            Text(
                text = weatherDescription(weather.weatherCode),
                style = MaterialTheme.typography.titleLarge,
                color = accent.secondary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = rangeLine,
                style = MaterialTheme.typography.bodyMedium,
                color = StandbyDim,
            )
        }
    }
}
