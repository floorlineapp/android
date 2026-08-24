package com.thefloor.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Floor brand tokens. Features never reference raw colors — always
 * FloorTheme.colors. Dark is the brand; a light theme is a token re-map away.
 */
@Immutable
data class FloorColors(
    val ink: Color = Color(0xFF0E1424),
    val surface: Color = Color(0xFF161D33),
    val surfaceAlt: Color = Color(0xFF1E2740),
    val border: Color = Color(0xFF2A3350),
    val borderSoft: Color = Color(0xFF212A46),
    val amber: Color = Color(0xFFF5A623),
    val amberSoft: Color = Color(0x24F5A623), // 14% alpha
    val teal: Color = Color(0xFF2DD9A3),
    val tealSoft: Color = Color(0x242DD9A3),
    val coral: Color = Color(0xFFFF6B5D),
    val coralSoft: Color = Color(0x24FF6B5D),
    val textPrimary: Color = Color(0xFFEDEFF6),
    val textSecondary: Color = Color(0xFF9AA3C0),
    val textMuted: Color = Color(0xFF6B7594),
    val onAmber: Color = Color(0xFF1A1205),
    /** True for the light palette — drives the Material scheme and system-bar icons. */
    val isLight: Boolean = false,
)

/** Dark is the brand default. */
fun darkFloorColors(): FloorColors = FloorColors()

/** Light theme — the same brand amber over inverted surfaces and text. */
fun lightFloorColors(): FloorColors = FloorColors(
    ink = Color(0xFFF5F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFEDF1F8),
    border = Color(0xFFD8DEE9),
    borderSoft = Color(0xFFE6EAF1),
    teal = Color(0xFF10B981),
    tealSoft = Color(0x2410B981),
    coral = Color(0xFFE5544A),
    coralSoft = Color(0x24E5544A),
    textPrimary = Color(0xFF141A2A),
    textSecondary = Color(0xFF56607A),
    textMuted = Color(0xFF8A93A8),
    isLight = true,
)

val LocalFloorColors = staticCompositionLocalOf { FloorColors() }
