package com.thefloor.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FloorSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val s: Dp = 12.dp,
    val m: Dp = 16.dp,
    val l: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    /** Standard screen gutter. */
    val gutter: Dp = 16.dp,
    /** Minimum accessible touch target. */
    val touchTarget: Dp = 48.dp,
)

val LocalFloorSpacing = staticCompositionLocalOf { FloorSpacing() }

object FloorTheme {
    val colors: FloorColors
        @Composable get() = LocalFloorColors.current
    val typography: FloorTypography
        @Composable get() = LocalFloorTypography.current
    val spacing: FloorSpacing
        @Composable get() = LocalFloorSpacing.current
}

/** Corner radii: cards 12, buttons 10, chips 8, sheets 20. */
private val FloorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun FloorTheme(content: @Composable () -> Unit) {
    val colors = FloorColors()
    val typography = FloorTypography()
    val spacing = FloorSpacing()

    // Material components inherit brand colors; custom components use FloorTheme.* directly.
    val materialScheme = darkColorScheme(
        primary = colors.amber,
        onPrimary = colors.onAmber,
        primaryContainer = colors.amberSoft,
        onPrimaryContainer = colors.amber,
        secondary = colors.teal,
        onSecondary = colors.ink,
        background = colors.ink,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.surfaceAlt,
        onSurfaceVariant = colors.textSecondary,
        surfaceContainer = colors.surface,
        surfaceContainerHigh = colors.surfaceAlt,
        surfaceContainerHighest = colors.surfaceAlt,
        outline = colors.border,
        outlineVariant = colors.borderSoft,
        error = colors.coral,
        onError = colors.ink,
    )

    CompositionLocalProvider(
        LocalFloorColors provides colors,
        LocalFloorTypography provides typography,
        LocalFloorSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            shapes = FloorShapes,
            content = content,
        )
    }
}
