package com.thefloor.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TimeAgoTest {

    private val now = Instant.parse("2026-08-10T12:00:00Z")

    @Test
    fun `relative buckets`() {
        assertEquals("now", TimeAgo.format("2026-08-10T11:59:40Z", now))
        assertEquals("5m", TimeAgo.format("2026-08-10T11:55:00Z", now))
        assertEquals("3h", TimeAgo.format("2026-08-10T09:00:00Z", now))
        assertEquals("2d", TimeAgo.format("2026-08-08T12:00:00Z", now))
        assertEquals("2w", TimeAgo.format("2026-07-27T12:00:00Z", now))
        assertEquals("1y", TimeAgo.format("2025-08-01T12:00:00Z", now))
    }

    @Test
    fun `bad input degrades to empty, never crashes`() {
        assertEquals("", TimeAgo.format("not-a-date", now))
        assertEquals("", TimeAgo.format("", now))
    }

    @Test
    fun `future timestamps render as now`() {
        assertEquals("now", TimeAgo.format("2026-08-10T12:05:00Z", now))
    }
}
