@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.thefloor.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.thefloor.app.R

/**
 * Brand type ramp — the single biggest driver of the prototype look.
 * Display = Archivo, body = IBM Plex Sans, data/eyebrows = IBM Plex Mono.
 *
 * Archivo and IBM Plex Sans ship as variable fonts (wght axis); we bind the
 * exact weights the design uses through FontVariation. IBM Plex Mono ships as
 * static Medium/SemiBold faces. Variable-font weight binding requires API 26+,
 * which matches the app's minSdk.
 */
private fun wght(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))

private val DisplayFamily: FontFamily = FontFamily(
    Font(R.font.archivo_var, weight = FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.archivo_var, weight = FontWeight.Medium, variationSettings = wght(500)),
    Font(R.font.archivo_var, weight = FontWeight.SemiBold, variationSettings = wght(600)),
    Font(R.font.archivo_var, weight = FontWeight.Bold, variationSettings = wght(700)),
    Font(R.font.archivo_var, weight = FontWeight.ExtraBold, variationSettings = wght(800)),
)

private val BodyFamily: FontFamily = FontFamily(
    Font(R.font.ibmplexsans_var, weight = FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.ibmplexsans_var, weight = FontWeight.Medium, variationSettings = wght(500)),
    Font(R.font.ibmplexsans_var, weight = FontWeight.SemiBold, variationSettings = wght(600)),
    Font(R.font.ibmplexsans_var, weight = FontWeight.Bold, variationSettings = wght(700)),
)

private val MonoFamily: FontFamily = FontFamily(
    Font(R.font.ibmplexmono_medium, weight = FontWeight.Medium),
    Font(R.font.ibmplexmono_semibold, weight = FontWeight.SemiBold),
)

@Immutable
data class FloorTypography(
    // Archivo display — big editorial headlines.
    val displayL: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp, lineHeight = 35.sp, letterSpacing = (-0.4).sp,
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 23.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.2).sp,
    ),
    val titleSm: TextStyle = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    // IBM Plex Sans body.
    val bodyL: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 23.sp,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.5f.sp, lineHeight = 20.sp,
    ),
    val bodyStrong: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.5f.sp, lineHeight = 20.sp,
    ),
    val label: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 17.sp,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    // IBM Plex Mono — eyebrows, tags, numeric data.
    val eyebrow: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
        fontSize = 10.5f.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp,
    ),
    val monoTag: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.8.sp,
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 19.sp,
    ),
    val monoL: TextStyle = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 26.sp,
    ),
)

val LocalFloorTypography = staticCompositionLocalOf { FloorTypography() }
