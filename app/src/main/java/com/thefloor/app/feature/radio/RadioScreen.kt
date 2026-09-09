package com.thefloor.app.feature.radio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorLiveDot
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Floor Radio. Playback goes through [RadioPlayerController] (ExoPlayer under
 * the hood), which takes its stream URL from server config — this screen is
 * real player UI over a real player; only the origin stream is pending.
 */
@HiltViewModel
class RadioViewModel @Inject constructor(
    private val controller: RadioPlayerController,
) : ViewModel() {

    val state = controller.state

    fun play() = controller.play()
    fun stop() = controller.stop()
}

/** The Global Handover — the hubs that pass the shift between them. */
private val clockCities = listOf(
    "Johannesburg" to "Africa/Johannesburg",
    "Manila" to "Asia/Manila",
    "Bogotá" to "America/Bogota",
    "Bengaluru" to "Asia/Kolkata",
    "Cairo" to "Africa/Cairo",
    "Tirana" to "Europe/Tirane",
)

private data class Show(val time: String, val name: String, val desc: String)

private val schedule = listOf(
    Show("05:30", "Morning Login", "Start the shift with original music, shout-outs and the five headlines worth knowing."),
    Show("08:00", "Between Calls", "Short, funny stories from real BPO life — designed for a coffee break, not a boardroom."),
    Show("11:00", "The Floor Briefing", "Contact-centre news, AI, CX, jobs and industry changes — sourced, credited and condensed."),
    Show("14:00", "Made on The Floor", "Original tracks submitted by agents, TLs and BPO people who make music after the shift."),
    Show("17:00", "The Handover", "One timezone signs off and another takes over. Johannesburg, Manila, Bogotá, Tirana and beyond."),
    Show("20:00", "Floor Sessions", "Interviews, creator sets, career stories and community conversations."),
    Show("23:00", "Night Shift", "Low-light energy, original music and company for the people keeping the world awake."),
)

private val listeningCities = listOf(
    "Manila, Philippines" to 1240,
    "Johannesburg, South Africa" to 890,
    "Cape Town, South Africa" to 640,
    "Bengaluru, India" to 610,
    "Cairo, Egypt" to 512,
    "Bogotá, Colombia" to 480,
)

@Composable
fun RadioScreen(viewModel: RadioViewModel = hiltViewModel()) {
    val playback by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Floor Radio") },
    ) { padding ->
        RadioBody(
            playback = playback,
            modifier = Modifier.padding(padding),
            onPlay = viewModel::play,
            onStop = viewModel::stop,
        )
    }
}

/** Stateless Radio body — internal so the screenshot suite can render it. */
@Composable
internal fun RadioBody(
    playback: RadioPlaybackState,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    val playing = (playback as? RadioPlaybackState.OnAir)?.playing == true
    val programName = (playback as? RadioPlaybackState.OnAir)?.programName ?: "Night Shift"

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(FloorTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FloorHero(
                eyebrow = "Floor Radio · Always on",
                eyebrowAccent = FloorAccent.CORAL,
                title = "When words stop, the shift keeps moving.",
                subtitle = "One station, every timezone. Music, industry briefings and the voices of the people behind every customer conversation.",
                actions = {
                    FloorPillButton(if (playing) "Stop" else "Listen live", onClick = { if (playing) onStop() else onPlay() })
                    FloorPillButton("Submit your music", onClick = {}, primary = false)
                },
            )
        }

        // On-air card with the transport control.
        item {
            val coral = FloorTheme.colors.coral
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = FloorTheme.colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, coral.copy(alpha = 0.35f)),
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
                            Text("ON AIR NOW", style = FloorTheme.typography.monoTag, color = coral)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(programName, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "For the ones awake when everyone else is asleep.",
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

        // The Global Handover — live clocks.
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

        // Schedule
        item {
            FloorSectionHeader(
                title = "Today's schedule",
                subtitle = "Seven shows, rolling across every timezone.",
            )
        }
        items(schedule, key = { it.time }) { show ->
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
                        Text(show.name, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(show.desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                }
            }
        }

        // Where people are listening
        item {
            FloorSectionHeader(title = "Listening right now", subtitle = "Across the global network.")
        }
        item {
            FloorCard(contentPadding = 4.dp) {
                listeningCities.forEach { (city, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FloorLiveDot()
                        Spacer(Modifier.width(10.dp))
                        Text(city, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                        Text("%,d".format(count), style = FloorTheme.typography.mono, color = FloorTheme.colors.teal)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
