package com.thefloor.app.feature.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.navigation.Routes

/**
 * Everything that is not one of the four permanent destinations, grouped by the
 * job it does — Belong & Recognition, Grow & Learn, Earn & Build — exactly as
 * the page-flow schematic groups the twelve non-nav sections.
 *
 * Walker is not listed here: it floats over every screen instead.
 */
@Composable
private fun GroupHeader(text: String, accent: FloorAccent) {
    Spacer(Modifier.height(18.dp))
    FloorEyebrow(text, accent = accent, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(6.dp))
}

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "More") },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            GroupHeader("Belong & recognition", FloorAccent.AMBER)
            FloorListItem(
                title = "My Profile",
                subtitle = "Identity, verification and your Recognition ladder",
                icon = Icons.Outlined.Person,
                onClick = { onNavigate(Routes.PROFILE) },
            )
            FloorListItem(
                title = "Pulse",
                subtitle = "The fast social feed — moments, not discussions",
                icon = Icons.Outlined.Bolt,
                onClick = { onNavigate(Routes.PULSE) },
            )
            FloorListItem(
                title = "Workplace Spotlight",
                subtitle = "Moderated showcase — unlocks at Floor Voice",
                icon = Icons.Outlined.Insights,
                onClick = { onNavigate(Routes.INSIGHTS) },
            )
            FloorListItem(
                title = "Events",
                subtitle = "Industry webinars and Floor Radio sessions",
                icon = Icons.Outlined.CalendarMonth,
                onClick = { onNavigate(Routes.EVENTS) },
            )

            GroupHeader("Grow & learn", FloorAccent.TEAL)
            FloorListItem(
                title = "Academy",
                subtitle = "Skills Passport, tracked learning and verification",
                icon = Icons.Outlined.School,
                onClick = { onNavigate(Routes.ACADEMY) },
            )
            FloorListItem(
                title = "Resources",
                subtitle = "Free instant toolbox — nothing tracked, nothing pointed",
                icon = Icons.Outlined.MenuBook,
                onClick = { onNavigate(Routes.RESOURCES) },
            )
            FloorListItem(
                title = "Employers & Jobs",
                subtitle = "Employer directory and the opportunity board",
                icon = Icons.Outlined.Work,
                onClick = { onNavigate(Routes.JOBS) },
            )

            GroupHeader("Earn & build", FloorAccent.CORAL)
            FloorListItem(
                title = "Rewards & Games",
                subtitle = "The Floor Points ledger, games and redeemables",
                icon = Icons.Outlined.EmojiEvents,
                onClick = { onNavigate(Routes.REWARDS) },
            )
            FloorListItem(
                title = "Invite & Grow",
                subtitle = "Referral record and Founding tiers — separate from points",
                icon = Icons.Outlined.CardGiftcard,
                onClick = { onNavigate(Routes.INVITE_EARN) },
            )
            FloorListItem(
                title = "Marketplace",
                subtitle = "Member deals across ten categories",
                icon = Icons.Outlined.Storefront,
                onClick = { onNavigate(Routes.MARKETPLACE) },
            )

            GroupHeader("The Floor", FloorAccent.FAINT)
            FloorListItem(
                title = "About The Floor",
                subtitle = "Vision and positioning",
                icon = Icons.Outlined.Info,
                onClick = { onNavigate(Routes.ABOUT) },
            )
            FloorListItem(
                title = "Settings",
                icon = Icons.Outlined.Settings,
                onClick = { onNavigate(Routes.SETTINGS) },
            )

            Spacer(Modifier.height(10.dp))
            Text(
                "Need help with any of it? The Walker button travels with you — tap it from any screen.",
                style = FloorTheme.typography.caption,
                color = FloorTheme.colors.textMuted,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
