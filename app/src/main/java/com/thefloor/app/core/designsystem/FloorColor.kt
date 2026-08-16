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
)

val LocalFloorColors = staticCompositionLocalOf { FloorColors() }
