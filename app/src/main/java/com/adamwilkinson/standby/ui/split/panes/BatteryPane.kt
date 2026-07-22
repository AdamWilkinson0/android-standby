package com.adamwilkinson.standby.ui.split.panes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamwilkinson.standby.ui.pages.BatteryBar
import com.adamwilkinson.standby.ui.theme.Inter
import com.adamwilkinson.standby.ui.theme.TABULAR_NUMS
import com.adamwilkinson.standby.vm.BatteryViewModel
import com.adamwilkinson.standby.vm.StandbyViewModels

@Composable
fun BatteryPane(
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = viewModel(factory = StandbyViewModels.Factory),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center,
    ) {
        status?.let {
            BatteryBar(
                status = it,
                percentStyle = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 72.sp,
                    fontFeatureSettings = TABULAR_NUMS,
                ),
                barHeight = 24.dp,
                showStatusText = true,
            )
        }
    }
}
