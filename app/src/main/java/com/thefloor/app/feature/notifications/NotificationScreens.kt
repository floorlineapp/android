package com.thefloor.app.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.NotificationRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorTextButton
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.AppNotification
import com.thefloor.app.core.network.PrefsDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val loading: Boolean = true,
    val items: List<AppNotification> = emptyList(),
    val unread: Int = 0,
    val error: String? = null,
    val prefs: PrefsDto? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    val state = MutableStateFlow(NotificationsUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.fetch()
                .onSuccess { (items, unread) ->
                    state.update { it.copy(loading = false, items = items, unread = unread, error = null) }
                }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun open(notification: AppNotification, navigate: (String) -> Unit) {
        analytics.track(Events.NOTIFICATION_OPENED, mapOf("type" to notification.type))
        viewModelScope.launch { repository.markRead(notification.id) }
        state.update { s ->
            s.copy(items = s.items.map { if (it.id == notification.id) it.copy(read = true) else it })
        }
        notification.deepLink?.let(navigate)
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markAllRead().onSuccess {
                state.update { s -> s.copy(items = s.items.map { it.copy(read = true) }, unread = 0) }
            }
        }
    }

    fun loadPrefs() {
        viewModelScope.launch {
            repository.prefs().onSuccess { prefs -> state.update { it.copy(loading = false, prefs = prefs) } }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun updatePrefs(prefs: PrefsDto) {
        state.update { it.copy(prefs = prefs) }
        viewModelScope.launch { repository.updatePrefs(prefs) }
    }
}

@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onOpenPrefs: () -> Unit,
    onOpenDeepLink: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = {
            FloorTopBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onOpenPrefs) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Notification settings", tint = FloorTheme.colors.textPrimary)
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 6, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.items.isEmpty() -> FloorEmptyState(
                title = "All caught up",
                message = "Community activity, referrals and rewards will show up here.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(modifier = Modifier.padding(padding)) {
                if (state.unread > 0) {
                    FloorTextButton(text = "Mark all as read", onClick = viewModel::markAllRead)
                }
                LazyColumn(
                    contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.items, key = { it.id }) { notification ->
                        FloorCard(onClick = { viewModel.open(notification, onOpenDeepLink) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!notification.read) {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(8.dp)
                                            .background(FloorTheme.colors.amber, CircleShape),
                                    )
                                }
                                Column {
                                    Text(notification.title, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
                                    Spacer(Modifier.height(2.dp))
                                    Text(notification.body, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val prefCategories = listOf(
    "community" to "Community activity",
    "replies" to "Replies & mentions",
    "referrals" to "Referrals & milestones",
    "rewards" to "Rewards",
    "jobs" to "Job recommendations",
    "radio" to "Radio & events",
)

@Composable
fun NotificationPrefsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadPrefs() }
    val prefs = state.prefs

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Notification preferences", onBack = onBack) },
    ) { padding ->
        when {
            prefs == null && state.error == null -> SkeletonList(rows = 4, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::loadPrefs,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PrefRow(
                    label = "All notifications",
                    checked = prefs!!.master,
                    onChange = { viewModel.updatePrefs(prefs.copy(master = it)) },
                )
                prefCategories.forEach { (key, label) ->
                    PrefRow(
                        label = label,
                        checked = prefs.categories[key] ?: true,
                        enabled = prefs.master,
                        onChange = { checked ->
                            viewModel.updatePrefs(
                                prefs.copy(categories = prefs.categories + (key to checked))
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PrefRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = FloorTheme.typography.bodyL,
            color = if (enabled) FloorTheme.colors.textPrimary else FloorTheme.colors.textMuted,
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FloorTheme.colors.onAmber,
                checkedTrackColor = FloorTheme.colors.amber,
                uncheckedTrackColor = FloorTheme.colors.surfaceAlt,
            ),
        )
    }
}
