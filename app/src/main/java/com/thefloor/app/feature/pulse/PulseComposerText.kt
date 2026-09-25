package com.thefloor.app.feature.pulse

/** The text maths behind the composer, kept pure so it can be tested without a ViewModel, a repository or a… */
object PulseComposerText {
    const val MAX_CHARS = 220

    data class Result(val text: String, val caret: Int)

    fun insert(text: String, selectionStart: Int, selectionEnd: Int, insert: String): Result {
        val start = selectionStart.coerceIn(0, text.length)
        val end = selectionEnd.coerceIn(start, text.length)
        val merged = text.substring(0, start) + insert + text.substring(end)
        val clipped = merged.take(MAX_CHARS)
        return Result(clipped, (start + insert.length).coerceAtMost(clipped.length))
    }
}
