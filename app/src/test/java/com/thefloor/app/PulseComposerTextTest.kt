package com.thefloor.app

import com.thefloor.app.feature.pulse.PulseComposerText
import org.junit.Assert.assertEquals
import org.junit.Test

/** The reported bug was that tapping an emoji "doesn't reflect in the live text input while typing". */
class PulseComposerTextTest {
    @Test
    fun `an emoji lands at the caret, not at the end`() {
        val r = PulseComposerText.insert("Long call  but we got there", 10, 10, "🔥")
        assertEquals("Long call 🔥 but we got there", r.text)
    }

    @Test
    fun `the caret ends up after what was inserted`() {
        val r = PulseComposerText.insert("ab", 1, 1, "💯")
        assertEquals("a💯b", r.text)
        assertEquals(1 + "💯".length, r.caret)
    }

    @Test
    fun `inserting over a selection replaces it`() {
        val r = PulseComposerText.insert("hello world", 6, 11, "😂")
        assertEquals("hello 😂", r.text)
    }

    @Test
    fun `an empty field still works`() {
        val r = PulseComposerText.insert("", 0, 0, "❤️")
        assertEquals("❤️", r.text)
        assertEquals(r.text.length, r.caret)
    }

    @Test
    fun `the 220 character ceiling holds and the caret stays inside the text`() {
        val full = "x".repeat(PulseComposerText.MAX_CHARS)
        val r = PulseComposerText.insert(full, full.length, full.length, "🔥")
        assertEquals(PulseComposerText.MAX_CHARS, r.text.length)
        assert(r.caret <= r.text.length)
    }

    @Test
    fun `an out of range selection cannot crash or corrupt the text`() {
        val r = PulseComposerText.insert("abc", 99, 120, "!")
        assertEquals("abc!", r.text)
        assertEquals(4, r.caret)
    }
}
