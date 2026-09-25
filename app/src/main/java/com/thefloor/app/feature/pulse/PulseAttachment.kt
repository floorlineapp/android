package com.thefloor.app.feature.pulse

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Something actually attached to a Pulse. */
sealed interface PulseAttachment {
    /** What the composer chip shows. */
    val label: String

    /** Sent as pulse_posts.media_type. */
    val mediaType: String

    /** Sent as pulse_posts.media_url — a local uri today, a storage url once the bucket exists. */
    val url: String

    data class Photo(override val url: String) : PulseAttachment {
        override val label = "Photo attached"
        override val mediaType = "photo"
    }

    data class Voice(override val url: String, val seconds: Int) : PulseAttachment {
        override val label = "Voice note · %d:%02d".format(seconds / 60, seconds % 60)
        override val mediaType = "voice"
    }
}

/** A thin wrapper over MediaRecorder that never claims success it did not have. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var target: File? = null
    private var startedAt: Long = 0

    val isRecording: Boolean get() = recorder != null

    fun start(): Boolean {
        if (recorder != null) return false
        val file = File(context.cacheDir, "pulse-voice-${System.currentTimeMillis()}.m4a")
        val created = runCatching {
            @Suppress("DEPRECATION")
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            r
        }.getOrNull() ?: return false

        recorder = created
        target = file
        startedAt = System.currentTimeMillis()
        return true
    }

    /** Returns the finished note, or null when nothing usable was captured. */
    fun stop(): PulseAttachment.Voice? {
        val r = recorder ?: return null
        val file = target
        recorder = null
        target = null
        runCatching { r.stop() }.onFailure {
            runCatching { r.release() }
            runCatching { file?.delete() }
            return null
        }
        runCatching { r.release() }
        if (file == null || !file.exists() || file.length() <= 0L) return null
        val seconds = ((System.currentTimeMillis() - startedAt) / 1000).toInt().coerceAtLeast(1)
        return PulseAttachment.Voice(android.net.Uri.fromFile(file).toString(), seconds)
    }

    fun cancel() {
        val r = recorder ?: return
        recorder = null
        runCatching { r.stop() }
        runCatching { r.release() }
        runCatching { target?.delete() }
        target = null
    }
}
