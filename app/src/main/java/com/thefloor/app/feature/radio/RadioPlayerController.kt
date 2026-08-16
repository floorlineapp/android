package com.thefloor.app.feature.radio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RadioPlaybackState {
    data object Stopped : RadioPlaybackState
    data object Loading : RadioPlaybackState
    data class OnAir(val playing: Boolean, val programName: String?) : RadioPlaybackState
    data class Error(val message: String) : RadioPlaybackState
}

/**
 * Real ExoPlayer-backed live-stream controller. The stream URL comes from
 * `GET /v1/radio/now` server config (placeholder until the streaming origin
 * exists — the flag stays off in production until then).
 *
 * Known scope cut (Phase 3): playback continues across screens in-app, but
 * true background audio needs a MediaSessionService + foreground notification;
 * the integration point is this class.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Singleton
class RadioPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow<RadioPlaybackState>(RadioPlaybackState.Stopped)
    val state: StateFlow<RadioPlaybackState> = _state

    private var player: ExoPlayer? = null
    private var streamUrl: String = DEFAULT_STREAM_URL
    private var programName: String? = null

    fun configure(url: String, program: String?) {
        streamUrl = url
        programName = program
    }

    fun play() {
        _state.value = RadioPlaybackState.Loading
        val exo = player ?: ExoPlayer.Builder(context).build().also { built ->
            built.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> _state.value = RadioPlaybackState.OnAir(built.playWhenReady, programName)
                        Player.STATE_BUFFERING -> _state.value = RadioPlaybackState.Loading
                        Player.STATE_ENDED, Player.STATE_IDLE -> Unit
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (_state.value is RadioPlaybackState.OnAir || isPlaying) {
                        _state.value = RadioPlaybackState.OnAir(isPlaying, programName)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Timber.w(error, "Radio playback error")
                    _state.value = RadioPlaybackState.Error("Couldn't reach the stream. Check your connection and try again.")
                }
            })
            player = built
        }
        exo.setMediaItem(MediaItem.fromUri(streamUrl))
        exo.prepare()
        exo.playWhenReady = true
    }

    fun stop() {
        player?.stop()
        _state.value = RadioPlaybackState.Stopped
    }

    fun release() {
        player?.release()
        player = null
        _state.value = RadioPlaybackState.Stopped
    }

    private companion object {
        // Placeholder — replaced by /v1/radio/now config when the origin exists.
        const val DEFAULT_STREAM_URL = "https://stream.thefloor.example/live.m3u8"
    }
}
