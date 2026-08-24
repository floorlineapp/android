package com.thefloor.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.datastore.ThemeMode

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

/** Resolves a [ThemeMode] to an effective dark/light state, honoring the System option. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun FloorTheme(
    mode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val dark = mode.isDark()
    val colors = if (dark) darkFloorColors() else lightFloorColors()
    val typography = FloorTypography()
    val spacing = FloorSpacing()

    // Custom components read FloorTheme.colors directly; Material components inherit the
    // brand mapping below. The light/dark base only fills roles we don't override.
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val materialScheme = base.copy(
        primary = colors.amber,
        onPrimary = colors.onAmber,
        primaryContainer = colors.amberSoft,
        onPrimaryContainer = colors.amber,
        secondary = colors.teal,
        onSecondary = if (dark) colors.ink else Color.White,
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
        onError = if (dark) colors.ink else Color.White,
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
