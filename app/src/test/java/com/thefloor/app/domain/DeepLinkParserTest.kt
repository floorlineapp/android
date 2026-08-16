package com.thefloor.app.domain

import com.thefloor.app.domain.DeepLinkParser.Target
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkParserTest {

    @Test
    fun `invite https link parses and uppercases code`() {
        assertEquals(
            Target.Invite("ABC123XY"),
            DeepLinkParser.parse("https://thefloor.example/invite/abc123xy"),
        )
    }

    @Test
    fun `invite custom scheme parses`() {
        assertEquals(Target.Invite("ABC123XY"), DeepLinkParser.parse("thefloor://invite/ABC123XY"))
        assertEquals(Target.InviteEarn, DeepLinkParser.parse("thefloor://invite"))
    }

    @Test
    fun `all entity links parse`() {
        assertEquals(Target.Floor("c-1"), DeepLinkParser.parse("https://thefloor.example/floor/c-1"))
        assertEquals(Target.TalkPost("p-1"), DeepLinkParser.parse("https://thefloor.example/talk/p-1"))
        assertEquals(Target.Job("j-1"), DeepLinkParser.parse("https://thefloor.example/job/j-1"))
        assertEquals(Target.Course("c-1"), DeepLinkParser.parse("https://thefloor.example/course/c-1"))
        assertEquals(Target.Deal("d-1"), DeepLinkParser.parse("https://thefloor.example/marketplace/d-1"))
        assertEquals(Target.Profile("u-1"), DeepLinkParser.parse("https://thefloor.example/profile/u-1"))
        assertEquals(Target.ResetPassword("tok"), DeepLinkParser.parse("https://thefloor.example/reset/tok"))
    }

    @Test
    fun `own-profile editor link is not mistaken for a member profile`() {
        assertEquals(Target.ProfileEdit, DeepLinkParser.parse("thefloor://profile/edit"))
        assertEquals(Target.ProfileEdit, DeepLinkParser.parse("https://thefloor.example/profile/edit"))
        assertEquals(Target.Profile("u-1"), DeepLinkParser.parse("thefloor://profile/u-1"))
    }

    @Test
    fun `verify email link carries its token`() {
        assertEquals(
            Target.VerifyEmail("tok123"),
            DeepLinkParser.parse("https://thefloor.example/verify/tok123"),
        )
    }

    @Test
    fun `foreign hosts are rejected to Home`() {
        assertEquals(Target.Home, DeepLinkParser.parse("https://evil.example/invite/ABC123XY"))
    }

    @Test
    fun `query strings are ignored`() {
        assertEquals(
            Target.Invite("ABC123XY"),
            DeepLinkParser.parse("https://thefloor.example/invite/ABC123XY?utm_source=x"),
        )
    }

    @Test
    fun `garbage input never crashes`() {
        assertEquals(Target.Home, DeepLinkParser.parse(""))
        assertEquals(Target.Home, DeepLinkParser.parse("not a uri"))
        assertEquals(Target.Home, DeepLinkParser.parse("https://thefloor.example/unknown/thing/extra"))
    }
}
