package com.adamwilkinson.standby.ui.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.adamwilkinson.standby.ui.split.PaneWidget
import com.adamwilkinson.standby.ui.theme.StandbyDim

/**
 * Long-press a split pane and pick what lives there — a discoverable
 * shortcut for the vertical swipe.
 */
@Composable
fun PanePickerOverlay(
    visible: Boolean,
    selected: PaneWidget,
    onSelect: (PaneWidget) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        ) + fadeIn(),
        exit = scaleOut(targetScale = 0.92f) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141414))
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PaneWidget.All.forEach { widget ->
                    val active = widget == selected
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (active) Color(0xFF262626) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (active) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                onSelect(widget)
                                onDismiss()
                            }
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                    ) {
                        Text(
                            text = widget.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (active) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                StandbyDim
                            },
                        )
                    }
                }
            }
        }
    }
}
