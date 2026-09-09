package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thefloor.app.R
import com.thefloor.app.core.designsystem.FloorTheme

/** The amber F mark on its own — traced from the brand artwork. */
@Composable
fun FloorLogoMark(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Image(
        painter = painterResource(R.drawable.ic_floor_mark),
        contentDescription = "The Floor",
        modifier = modifier.height(size).width(size * 0.86f),
    )
}

/**
 * Full brand lockup: the F mark followed by THE FLOOR. Used in the Home app bar
 * and on the welcome screen. The wordmark colour follows the theme so it stays
 * readable in light mode.
 */
@Composable
fun FloorWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 22.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        FloorLogoMark(size = markSize)
        Spacer(Modifier.width(9.dp))
        Text(
            "THE FLOOR",
            style = FloorTheme.typography.displayL.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
            ),
            color = FloorTheme.colors.textPrimary,
        )
    }
}

/**
 * Community photography, matched to the web prototype's own imagery so the
 * native Floor page reads the same. Loaded at runtime; the card shows a brand
 * gradient until the image arrives (and if it never does).
 */
private const val UNSPLASH = "https://images.unsplash.com/"
private const val PARAMS = "?w=800&q=70&auto=format&fit=crop"

private val floorPhotos: List<Pair<String, String>> = listOf(
    "south africa" to "photo-1602578984228-c98a9b995f3e",
    "philippines" to "photo-1623518761090-089a985ab17f",
    "colombia" to "photo-1681145553138-816eb004b846",
    "india" to "photo-1545562083-c583d014b4f2",
    "remote" to "photo-1616531770192-6eaea74c2456",
    "mexico" to "photo-1591049433264-618fa2f4558f",
    "poland" to "photo-1651062108412-36a68f3748dd",
    "jamaica" to "photo-1530225029356-e301a685e6b1",
    "albania" to "photo-1742243785500-d65256706485",
    "egypt" to "photo-1568322445389-f64ac2515020",
    "brazil" to "photo-1763110805416-22e7a6fffa58",
    "united states" to "photo-1760974015791-cf0dd376aa18",
    "night shift" to "photo-1544202482-5e970f02d8e7",
    "new agent" to "photo-1758518729459-235dcaadc611",
    "customer service" to "photo-1681164314348-1cab4b4eb17f",
)

/** Best-effort photo for a community name; null when nothing matches. */
fun floorPhotoUrl(name: String): String? {
    val n = name.lowercase()
    val hit = floorPhotos.firstOrNull { n.contains(it.first) } ?: return null
    return "$UNSPLASH${hit.second}$PARAMS"
}
