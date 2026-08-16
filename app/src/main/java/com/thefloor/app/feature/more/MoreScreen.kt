package com.thefloor.app.feature.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.data.ConfigRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val flags = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        viewModelScope.launch { flags.value = configRepository.flags() }
    }
}

@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    viewModel: MoreViewModel = hiltViewModel(),
) {
    val flags by viewModel.flags.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "More") },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            FloorListItem(
                title = "Rewards & Games",
                subtitle = "Credits, competitions, leaderboards",
                icon = Icons.Outlined.EmojiEvents,
                onClick = { onNavigate(Routes.REWARDS) },
            )
            FloorListItem(
                title = "Invite & Earn",
                subtitle = "Bring your floor with you — free to refer",
                icon = Icons.Outlined.CardGiftcard,
                onClick = { onNavigate(Routes.INVITE_EARN) },
            )
            FloorListItem(
                title = "Marketplace",
                subtitle = if (flags["marketplace"] == true) "Partner deals" else "Coming soon",
                icon = Icons.Outlined.Storefront,
                onClick = { onNavigate(Routes.MARKETPLACE) },
            )
            FloorListItem(
                title = "Academy",
                subtitle = if (flags["academy"] == true) "Courses & career growth" else "Coming soon",
                icon = Icons.Outlined.School,
                onClick = { onNavigate(Routes.ACADEMY) },
            )
            FloorListItem(
                title = "Employers & Jobs",
                subtitle = if (flags["jobs"] == true) "Verified employers hiring now" else "Coming soon",
                icon = Icons.Outlined.Work,
                onClick = { onNavigate(Routes.JOBS) },
            )
            FloorListItem(
                title = "Workplace Insights",
                subtitle = if (flags["insights"] == true) "How floors compare" else "Coming soon",
                icon = Icons.Outlined.Insights,
                onClick = { onNavigate(Routes.INSIGHTS) },
            )
            FloorListItem(
                title = "Profile",
                icon = Icons.Outlined.Person,
                onClick = { onNavigate(Routes.PROFILE) },
            )
            FloorListItem(
                title = "Settings",
                icon = Icons.Outlined.Settings,
                onClick = { onNavigate(Routes.SETTINGS) },
            )
            FloorListItem(
                title = "Help",
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                onClick = { onNavigate(Routes.INVITE_FAQ) },
            )
            FloorListItem(
                title = "About The Floor",
                icon = Icons.Outlined.Info,
                onClick = { onNavigate(Routes.ABOUT) },
            )
            Text(
                "The global home of the people behind every customer conversation.",
                style = FloorTheme.typography.caption,
                color = FloorTheme.colors.textMuted,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
