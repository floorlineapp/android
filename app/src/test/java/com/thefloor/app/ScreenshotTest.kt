package com.thefloor.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.thefloor.app.core.datastore.ThemeMode
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorAvatar
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorKpiCard
import com.thefloor.app.core.designsystem.components.FloorLiveRoomCard
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTile
import com.thefloor.app.core.designsystem.components.FloorVerifiedBadge
import com.thefloor.app.core.designsystem.components.FloorWordmark
import com.thefloor.app.feature.auth.WelcomeScreen
import com.thefloor.app.feature.pages.AboutScreen
import com.thefloor.app.feature.pages.AcademyScreen
import com.thefloor.app.feature.pages.EventsScreen
import com.thefloor.app.feature.pages.InsightsScreen
import com.thefloor.app.feature.pages.JobsScreen
import com.thefloor.app.feature.pages.MarketplaceScreen
import com.thefloor.app.feature.pages.ResourcesScreen
import com.thefloor.app.feature.pages.WellbeingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders each screen to a PNG on the JVM so the UI can actually be reviewed.
 * The viewport is deliberately tall so most of each scrolling page lands in one
 * frame. Snapshots are written to /screenshots and published by CI.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h1900dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun snap(name: String, dark: Boolean = true, content: @Composable () -> Unit) {
        compose.setContent {
            FloorTheme(mode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                Surface(color = FloorTheme.colors.ink, modifier = Modifier.fillMaxSize()) { content() }
            }
        }
        compose.onRoot().captureRoboImage("$name.png")
    }

    // ---------- editorial pages ----------
    @Test fun insights() = snap("page_insights") { InsightsScreen {} }
    @Test fun academy() = snap("page_academy") { AcademyScreen {} }
    @Test fun events() = snap("page_events") { EventsScreen {} }
    @Test fun marketplace() = snap("page_marketplace") { MarketplaceScreen {} }
    @Test fun resources() = snap("page_resources") { ResourcesScreen {} }
    @Test fun jobs() = snap("page_jobs") { JobsScreen {} }
    @Test fun about() = snap("page_about") { AboutScreen {} }
    @Test fun wellbeing() = snap("page_wellbeing") { WellbeingScreen {} }

    // ---------- brand / auth ----------
    @Test fun welcome() = snap("page_welcome") { WelcomeScreen({}, {}) }
    @Test fun welcomeLight() = snap("page_welcome_light", dark = false) { WelcomeScreen({}, {}) }

    // ---------- design-system gallery, both themes ----------
    @Test fun galleryDark() = snap("gallery_dark") { Gallery() }
    @Test fun galleryLight() = snap("gallery_light", dark = false) { Gallery() }

    @Test fun academyLight() = snap("page_academy_light", dark = false) { AcademyScreen {} }
    @Test fun aboutLight() = snap("page_about_light", dark = false) { AboutScreen {} }
}

@Composable
private fun Gallery() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FloorWordmark()
        FloorHero(
            eyebrow = "The Floor · Global Community",
            title = "Good afternoon shift, Naledi.",
            subtitle = "Wherever you work, whatever shift you're on — you're never alone on The Floor.",
            actions = {
                FloorPillButton("Find your Floor", onClick = {}, leadingIcon = Icons.Filled.Groups)
                FloorPillButton("Floor Radio", onClick = {}, primary = false, leadingIcon = Icons.Filled.Headphones)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloorKpiCard(Icons.Filled.Groups, "2,347", "people online now", Modifier.weight(1f), FloorAccent.TEAL)
            FloorKpiCard(Icons.Filled.Headphones, "12,480", "Floor points", Modifier.weight(1f), FloorAccent.AMBER)
        }
        FloorSectionHeader(title = "Jump back in", subtitle = "Your shift. Your Floor.", linkText = "See all", onLink = {})
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloorTile(Icons.Filled.Groups, "The Floor", "Your community by country and shift.", {}, Modifier.weight(1f), FloorAccent.AMBER)
            FloorTile(Icons.Filled.Headphones, "Floor Radio", "When words stop, the shift keeps moving.", {}, Modifier.weight(1f), FloorAccent.CORAL)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FloorAvatar("Naledi M.", ring = FloorAccent.TEAL)
            FloorAvatar("Thabo N.", ring = FloorAccent.AMBER)
            FloorVerifiedBadge()
        }
        FloorLiveRoomCard(
            icon = Icons.Filled.Headphones,
            topic = "Music on shift",
            desc = "What's in your headset right now? Playlists, new finds, and Floor Radio requests.",
            onlineText = "67 online now",
            onClick = {},
        )
    }
}
