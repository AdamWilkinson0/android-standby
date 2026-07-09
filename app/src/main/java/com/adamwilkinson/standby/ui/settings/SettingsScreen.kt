package com.adamwilkinson.standby.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.settings.StandbySettings
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFont
import com.adamwilkinson.standby.ui.theme.AccentPreset
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.vm.SettingsViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        settings?.let { current ->
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LargeTitle("Settings")
                    OutlinedButton(onClick = onClose) { Text("Done") }
                }

                SettingsGroup(header = "Appearance") {
                    Text(
                        text = "Accent",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 18.dp, top = 12.dp),
                    )
                    AccentSwatchRow(
                        selected = AccentPreset.fromId(current.accentId),
                        onSelect = viewModel::setAccent,
                    )
                    GroupDivider()
                    Text(
                        text = "Clock face",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 18.dp, top = 12.dp),
                    )
                    ChipRow(
                        options = ClockFaceStyle.entries.toList(),
                        selected = ClockFaceStyle.fromId(current.clockFaceId),
                        label = { it.label },
                        onSelect = viewModel::setClockFace,
                    )
                    GroupDivider()
                    Text(
                        text = "Clock font",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 18.dp, top = 12.dp),
                    )
                    ChipRow(
                        options = ClockFont.entries.toList(),
                        selected = ClockFont.fromId(current.clockFontId),
                        label = { it.label },
                        onSelect = viewModel::setClockFont,
                    )
                    Text(
                        text = "Tip: long-press the clock to customise it in place.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StandbyDim,
                        modifier = Modifier.padding(start = 18.dp, bottom = 12.dp),
                    )
                }

                SettingsGroup(header = "Widgets") {
                    val pages = StandbyPage.Default.filter { it != StandbyPage.Split }
                    pages.forEachIndexed { index, page ->
                        val enabled = page in StandbyPage.fromIds(current.pageIds)
                        ToggleRow(
                            label = page.label,
                            checked = enabled,
                            onCheckedChange = { viewModel.setPageEnabled(page, it) },
                        )
                        if (index != pages.lastIndex) GroupDivider()
                    }
                }

                SettingsGroup(header = "Now Playing") {
                    ToggleRow(
                        label = "Auto show Now Playing",
                        subtitle = "Split the screen when media starts playing",
                        checked = current.autoSplitMedia,
                        onCheckedChange = viewModel::setAutoSplitMedia,
                    )
                }

                SettingsGroup(header = "Weather") {
                    ToggleRow(
                        label = "Fahrenheit",
                        checked = current.useFahrenheit,
                        onCheckedChange = viewModel::setUseFahrenheit,
                    )
                    GroupDivider()
                    CityRow(current, viewModel)
                }

                SettingsGroup(header = "Display") {
                    ToggleRow(
                        label = "Night dimming",
                        subtitle = "10 pm – 7 am",
                        checked = current.nightDimEnabled,
                        onCheckedChange = viewModel::setNightDimEnabled,
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CityRow(settings: StandbySettings, viewModel: SettingsViewModel) {
    var city by rememberSaveable { mutableStateOf("") }
    var notFound by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Location: " + (settings.manualCityName ?: "Device location"),
            style = MaterialTheme.typography.bodyMedium,
            color = StandbyDim,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    notFound = false
                },
                placeholder = { Text("Change city") },
                singleLine = true,
                isError = notFound,
                modifier = Modifier.width(240.dp),
            )
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                enabled = city.isNotBlank(),
                onClick = {
                    viewModel.setCity(city) { found ->
                        notFound = !found
                        if (found) city = ""
                    }
                },
            ) {
                Text("Set")
            }
            if (settings.manualCityName != null) {
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = viewModel::useDeviceLocation) {
                    Text("Use device location")
                }
            }
        }
        if (notFound) {
            Text(
                text = "City not found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
