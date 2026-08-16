package com.thefloor.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Brand type ramp. Display = Archivo, body = IBM Plex Sans, data = IBM Plex Mono.
 *
 * TO ACTIVATE THE BRAND FONTS: drop the licensed TTFs into res/font/
 * (archivo_bold.ttf, archivo_semibold.ttf, ibmplexsans_regular.ttf,
 * ibmplexsans_medium.ttf, ibmplexmono_medium.ttf) and replace the three
 * FontFamily vals below with Font(R.font...) families. Until then the ramp
 * runs on system faces with matching weights/metrics, so nothing else changes.
 */
private val DisplayFamily: FontFamily = FontFamily.SansSerif
private val BodyFamily: FontFamily = FontFamily.SansSerif
private val MonoFamily: FontFamily = FontFamily.Monospace

@Immutable
data class FloorTypography(
    val displayL: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 40.sp,
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    val bodyL: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    val label: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 20.sp,
    ),
    val monoL: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 26.sp,
    ),
)

val LocalFloorTypography = staticCompositionLocalOf { FloorTypography() }
