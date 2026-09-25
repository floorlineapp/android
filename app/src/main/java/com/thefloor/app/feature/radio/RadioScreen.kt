package com.thefloor.app.feature.radio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.datastore.RadioRegionStore
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorLiveDot
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** Floor Radio — six regional feeds, each with its own host, now-playing show, daily schedule and live chat room. */
@HiltViewModel
class RadioViewModel @Inject constructor(
    private val controller: RadioPlayerController,
    private val regionStore: RadioRegionStore,
    private val walkerBus: com.thefloor.app.core.walker.WalkerBus,
) : ViewModel() {
    /** Submitting music is a conversation, not a form. */
    fun submitMusic() = walkerBus.open()

    val state = controller.state

    val regionKey: StateFlow<String> = regionStore.regionKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "global")

    fun selectRegion(key: String) {
        viewModelScope.launch { regionStore.setRegion(key) }
    }

    fun play() = controller.play()
    fun stop() = controller.stop()
}

/** The Global Handover — the hubs that pass the shift between them. */
private val clockCities = listOf(
    "Johannesburg" to "Africa/Johannesburg",
    "Manila" to "Asia/Manila",
    "Bogotá" to "America/Bogota",
    "Bengaluru" to "Asia/Kolkata",
    "Kraków" to "Europe/Warsaw",
    "Cairo" to "Africa/Cairo",
)

/** Seeded chat for the region room — distinct from Talk's Live Rooms. */
private fun seedChat(region: RadioRegion): List<Pair<String, String>> = when (region.key) {
    "philippines" -> listOf(
        "Joan D." to "Graveyard Gold starts in ten. Requests in now.",
        "Mika R." to "Can we get something loud? Third escalation in a row.",
        "Ryan T." to "OPM Hour was unreal last night.",
    )
    "africa" -> listOf(
        "Lerato K." to "Joburg Drive is on. Cape Town, you're up next.",
        "Sipho D." to "Amapiano block please 🙏",
        "Amara O." to "Nairobi checking in, night shift crew.",
    )
    "india" -> listOf(
        "Rohan M." to "The Late Queue tonight — covering US hours with you.",
        "Priya S." to "Tech Support episode was genuinely useful today.",
    )
    "uk-europe" -> listOf(
        "Ana P." to "Switching to Portuguese for the next set.",
        "Marek W." to "Kraków floor is listening.",
    )
    "americas" -> listOf(
        "Camila R." to "Bogotá Sessions live in twenty minutes.",
        "Diego F." to "Guadalajara here, turning it up.",
    )
    else -> listOf(
        "The Floor Collective" to "The Handover is live. Johannesburg signing off, Manila taking over.",
        "Naledi M." to "Best hour of the day.",
        "Owen K." to "Every timezone in one room. Still gets me.",
    )
}

@Composable
fun RadioScreen(viewModel: RadioViewModel = hiltViewModel()) {
    val playback by viewModel.state.collectAsStateWithLifecycle()
    val regionKey by viewModel.regionKey.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Floor Radio") },
    ) { padding ->
        RadioBody(
            playback = playback,
            regionKey = regionKey,
            modifier = Modifier.padding(padding),
            onPlay = viewModel::play,
            onStop = viewModel::stop,
            onSelectRegion = viewModel::selectRegion,
            onSubmitMusic = viewModel::submitMusic,
        )
    }
}

/** Stateless Radio body — internal so the screenshot suite can render it. */
@Composable
internal fun RadioBody(
    playback: RadioPlaybackState,
    modifier: Modifier = Modifier,
    regionKey: String = "global",
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {},
    onSelectRegion: (String) -> Unit = {},
    onSubmitMusic: () -> Unit = {},
) {
    val region = radioRegion(regionKey)
    val playing = (playback as? RadioPlaybackState.OnAir)?.playing == true
    val programName = (playback as? RadioPlaybackState.OnAir)?.programName ?: region.nowPlaying
    var chatOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = FloorTheme.spacing.gutter,
            end = FloorTheme.spacing.gutter,
            top = FloorTheme.spacing.gutter,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FloorHero(
                eyebrow = "Always-on live audio · 6 regions",
                eyebrowAccent = FloorAccent.CORAL,
                title = "When words stop, the shift keeps moving.",
                subtitle = "Six regional feeds, each with its own host, show and schedule — plus a " +
                    "chat room for the people listening with you.",
                actions = {
                    FloorPillButton(if (playing) "Stop" else "Listen live", onClick = { if (playing) onStop() else onPlay() })
                    FloorPillButton("Submit your music", onClick = onSubmitMusic, primary = false)
                },
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RADIO_REGIONS) { r ->
                    FloorChip(text = r.label, selected = r.key == region.key, onClick = { onSelectRegion(r.key) })
                }
            }
        }

        item {
            val coral = FloorTheme.colors.coral
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = FloorTheme.colors.surface,
                border = BorderStroke(1.dp, coral.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(coral.copy(alpha = 0.11f), FloorTheme.colors.amber.copy(alpha = 0.07f)),
                            ),
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FloorLiveDot(color = coral)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "ON AIR — ${region.label.uppercase()}",
                                style = FloorTheme.typography.monoTag,
                                color = coral,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(programName, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Hosted by ${region.host} · ${region.city}",
                            style = FloorTheme.typography.monoTag,
                            color = FloorTheme.colors.textMuted,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            region.tagline,
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    when (playback) {
                        RadioPlaybackState.Loading -> CircularProgressIndicator(
                            color = FloorTheme.colors.amber,
                            modifier = Modifier.size(28.dp),
                        )
                        is RadioPlaybackState.Error -> FloorBadge("Off air", tone = BadgeTone.CORAL)
                        else -> Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = FloorTheme.colors.amber,
                        ) {
                            IconButton(onClick = { if (playing) onStop() else onPlay() }, modifier = Modifier.size(56.dp)) {
                                Icon(
                                    if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = if (playing) "Stop radio" else "Play radio",
                                    tint = FloorTheme.colors.onAmber,
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (playback is RadioPlaybackState.Error) {
            item {
                FloorCard {
                    Text("Radio is off the air right now", style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(playback.message, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                }
            }
        }

        item {
            FloorCard(onClick = { chatOpen = true }, contentPadding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorLiveDot()
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${region.label} live chat",
                            style = FloorTheme.typography.bodyStrong,
                            color = FloorTheme.colors.textPrimary,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Talking to whoever is listening to this feed right now.",
                            style = FloorTheme.typography.caption,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                    FloorBadge("%,d".format(region.listeners), tone = BadgeTone.TEAL)
                }
            }
        }

        item {
            FloorSectionHeader(
                title = "The Global Handover",
                subtitle = "Johannesburg clocks out. Manila takes The Floor.",
            )
        }
        item {
            val fmt = DateTimeFormatter.ofPattern("HH:mm")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                clockCities.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { (city, zone) ->
                            val now = runCatching {
                                ZonedDateTime.now(ZoneId.of(zone)).format(fmt)
                            }.getOrDefault("--:--")
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FloorTheme.colors.surfaceAlt)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                FloorEyebrow(city, accent = FloorAccent.FAINT)
                                Spacer(Modifier.height(2.dp))
                                Text(now, style = FloorTheme.typography.mono, color = FloorTheme.colors.textPrimary)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            FloorSectionHeader(
                title = "${region.label} — today's schedule",
                subtitle = "Times shown in ${region.city} local time.",
            )
        }
        items(region.schedule, key = { it.time + region.key }) { show ->
            FloorCard(contentPadding = 16.dp) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        show.time,
                        style = FloorTheme.typography.mono,
                        color = FloorTheme.colors.amber,
                        modifier = Modifier.width(56.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                show.name,
                                style = FloorTheme.typography.titleSm,
                                color = FloorTheme.colors.textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            if (show.name == region.nowPlaying) {
                                FloorBadge("ON AIR", tone = BadgeTone.CORAL)
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(show.desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                }
            }
        }

        item { FloorSectionHeader(title = "Listening right now", subtitle = "Across the six regional feeds.") }
        item {
            FloorCard(contentPadding = 4.dp) {
                RADIO_REGIONS.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FloorLiveDot(
                            color = if (r.key == region.key) FloorTheme.colors.coral else FloorTheme.colors.teal,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.label, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
                            Text(r.nowPlaying, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                        }
                        Text("%,d".format(r.listeners), style = FloorTheme.typography.mono, color = FloorTheme.colors.teal)
                    }
                }
            }
        }

        item {
            FloorInfoNote {
                Text(
                    "Floor Radio runs as a scheduled programme, not an open microphone — each " +
                        "region's segments are produced and timed to that region's shift pattern.",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }
        }
    }

    if (chatOpen) {
        RegionChatSheet(region = region, onDismiss = { chatOpen = false })
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun RegionChatSheet(region: RadioRegion, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val messages = remember(region.key) { mutableStateListOf(*seedChat(region).toTypedArray()) }
    var draft by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FloorTheme.colors.ink,
        contentColor = FloorTheme.colors.textPrimary,
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FloorLiveDot(color = FloorTheme.colors.coral)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${region.label} live chat",
                        style = FloorTheme.typography.title,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Text(
                        "${"%,d".format(region.listeners)} listening · ${region.nowPlaying}",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            LazyColumn(
                modifier = Modifier.heightIn(min = 180.dp, max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages.size) { i ->
                    val (who, what) = messages[i]
                    Column {
                        Text(who, style = FloorTheme.typography.label, color = FloorTheme.colors.amber)
                        Spacer(Modifier.height(2.dp))
                        Text(what, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FloorTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = "Say something",
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            messages.add("You" to draft.trim())
                            draft = ""
                        }
                    },
                    enabled = draft.isNotBlank(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (draft.isNotBlank()) FloorTheme.colors.amber else FloorTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}
