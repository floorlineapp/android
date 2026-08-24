package com.thefloor.app.feature.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Floor Radio. Playback goes through [RadioPlayerController] (ExoPlayer under
 * the hood), which takes its stream URL from server config — this screen is
 * real player UI over a real player; only the origin stream is pending.
 * Ships behind the `radio` feature flag (tab hidden when off).
 */
@HiltViewModel
class RadioViewModel @Inject constructor(
    private val controller: RadioPlayerController,
) : ViewModel() {

    val state = controller.state

    fun play() = controller.play()
    fun stop() = controller.stop()

    override fun onCleared() {
        // Keep playing across screens; release happens at app-process level.
        super.onCleared()
    }
}

@Composable
fun RadioScreen(
    viewModel: RadioViewModel = hiltViewModel(),
) {
    val playback by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Floor Radio") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val s = playback) {
                is RadioPlaybackState.OnAir, RadioPlaybackState.Loading, RadioPlaybackState.Stopped -> {
                    if (s is RadioPlaybackState.OnAir && s.playing) {
                        FloorBadge(text = "ON AIR", tone = BadgeTone.TEAL, showDot = true)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        (s as? RadioPlaybackState.OnAir)?.programName ?: "Floor Radio",
                        style = FloorTheme.typography.headline,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Music and voices for every shift.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                    Spacer(Modifier.height(40.dp))
                    if (s == RadioPlaybackState.Loading) {
                        CircularProgressIndicator(color = FloorTheme.colors.amber)
                    } else {
                        val playing = (s as? RadioPlaybackState.OnAir)?.playing == true
                        IconButton(
                            onClick = { if (playing) viewModel.stop() else viewModel.play() },
                            modifier = Modifier.size(72.dp),
                        ) {
                            Icon(
                                if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = if (playing) "Stop radio" else "Play radio",
                                tint = FloorTheme.colors.amber,
                                modifier = Modifier.size(56.dp),
                            )
                        }
                    }
                }
                is RadioPlaybackState.Error -> {
                    Text(
                        "Radio is off the air right now",
                        style = FloorTheme.typography.title,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(s.message, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    Spacer(Modifier.height(16.dp))
                    com.thefloor.app.core.designsystem.components.FloorSecondaryButton(
                        text = "Try again",
                        onClick = { viewModel.play() },
                    )
                }
            }
        }
    }
}
