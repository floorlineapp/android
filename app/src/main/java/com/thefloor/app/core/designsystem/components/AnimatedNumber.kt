package com.thefloor.app.core.designsystem.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.thefloor.app.core.designsystem.FloorTheme

/** A number that counts up to its new value instead of snapping to it. */
@Composable
fun FloorAnimatedNumber(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = FloorTheme.typography.displayL,
    color: Color = FloorTheme.colors.amber,
    prefix: String = "",
) {
    val animated by animateIntAsState(
        targetValue = value.toInt(),
        animationSpec = tween(durationMillis = 520, easing = LinearOutSlowInEasing),
        label = "floorNumber",
    )
    Text("$prefix%,d".format(animated), style = style, color = color, modifier = modifier)
}
