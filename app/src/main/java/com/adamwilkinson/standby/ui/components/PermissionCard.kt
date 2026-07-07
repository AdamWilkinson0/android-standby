package com.adamwilkinson.standby.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Quiet, rounded card used by pages whose permission has not been granted. */
@Composable
fun PermissionCard(
    title: String,
    body: String,
    buttonLabel: String,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 420.dp)
            .background(Color(0xFF111111), RoundedCornerShape(28.dp))
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onGrant) {
            Text(buttonLabel)
        }
    }
}
