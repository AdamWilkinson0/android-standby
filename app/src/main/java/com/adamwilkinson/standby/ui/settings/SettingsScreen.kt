package com.adamwilkinson.standby.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.data.settings.StandbySettings
import com.adamwilkinson.standby.ui.StandbyPage
import com.adamwilkinson.standby.ui.pages.clockfaces.ClockFaceStyle
import com.adamwilkinson.standby.ui.theme.StandbyAccent
import com.adamwilkinson.standby.ui.theme.StandbyDim
import com.adamwilkinson.standby.ui.theme.StandbyFaint
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
        contentAlignment = Alignment.Center,
    ) {
        settings?.let { current ->
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    OutlinedButton(onClick = onClose) { Text("Done") }
                }

                SectionLabel("CLOCK FACE")
                ClockFacePicker(current, viewModel)

                SectionLabel("PAGES")
                StandbyPage.Default.forEach { page ->
                    val enabled = page in StandbyPage.fromIds(current.pageIds)
                    ToggleRow(
                        label = page.label,
                        checked = enabled,
                        onCheckedChange = { viewModel.setPageEnabled(page, it) },
                    )
                }

                SectionLabel("WEATHER")
                ToggleRow(
                    label = "Fahrenheit",
                    checked = current.useFahrenheit,
                    onCheckedChange = viewModel::setUseFahrenheit,
                )
                CityRow(current, viewModel)

                SectionLabel("DISPLAY")
                ToggleRow(
                    label = "Night dimming (10 pm – 7 am)",
                    checked = current.nightDimEnabled,
                    onCheckedChange = viewModel::setNightDimEnabled,
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = StandbyFaint,
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = StandbyAccent.copy(alpha = 0.5f),
                checkedThumbColor = StandbyAccent,
            ),
        )
    }
}

@Composable
private fun ClockFacePicker(settings: StandbySettings, viewModel: SettingsViewModel) {
    val selected = ClockFaceStyle.fromId(settings.clockFaceId)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ClockFaceStyle.entries.forEach { style ->
            val active = style == selected
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (active) StandbyAccent else Color(0xFF2A2A2A),
                        shape = RoundedCornerShape(50),
                    )
                    .clickable { viewModel.setClockFace(style) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = style.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) StandbyAccent else StandbyDim,
                )
            }
        }
    }
}

@Composable
private fun CityRow(settings: StandbySettings, viewModel: SettingsViewModel) {
    var city by rememberSaveable { mutableStateOf("") }
    var notFound by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
