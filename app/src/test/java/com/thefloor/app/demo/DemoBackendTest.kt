package com.thefloor.app.demo

import com.thefloor.app.core.demo.DemoBackend
import com.thefloor.app.core.demo.DemoMode
import com.thefloor.app.core.network.CommentDto
import com.thefloor.app.core.network.CommunityDto
import com.thefloor.app.core.network.CommunityListDto
import com.thefloor.app.core.network.FaqItemDto
import com.thefloor.app.core.network.HomeDto
import com.thefloor.app.core.network.MembershipDto
import com.thefloor.app.core.network.MilestoneTierDto
import com.thefloor.app.core.network.PostDto
import com.thefloor.app.core.network.PostPageDto
import com.thefloor.app.core.network.ProfileDto
import com.thefloor.app.core.network.PulseDto
import com.thefloor.app.core.network.PulsePageDto
import com.thefloor.app.core.network.ReferralListDto
import com.thefloor.app.core.network.RewardTransactionsDto
import com.thefloor.app.core.network.RewardsSummaryDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The demo build is what gets handed to people, so its offline backend is held
 * to the same standard as the real one: every route a screen calls must return
 * something its DTO can actually parse.
 *
 * An earlier version matched on path substrings and ignored the HTTP method,
 * which returned a list where a single object was expected on seven different
 * flows — opening a discussion, opening a Floor, joining one, posting a
 * discussion, posting a Pulse, commenting, and the three Invite sub-screens.
 * Each of those is a case below, so the shape can never silently drift again.
 */
class DemoBackendTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val backend = DemoBackend()
    private val noQuery: (String) -> String? = { null }

    private fun get(vararg seg: String, query: (String) -> String? = noQuery) =
        backend.handle("GET", seg.toList(), query, "")

    private fun send(method: String, vararg seg: String, body: String = "") =
        backend.handle(method, seg.toList(), noQuery, body)

    private fun balance(): Long =
        json.decodeFromString(RewardsSummaryDto.serializer(), get("rewards", "summary")).creditsBalance

    // ---- the seven that were broken ------------------------------------

    @Test
    fun `opening a discussion returns one post, not a page`() {
        val feed = json.decodeFromString(PostPageDto.serializer(), get("talk", "posts"))
        val first = feed.items.first()
        val detail = json.decodeFromString(PostDto.serializer(), get("talk", "posts", first.id))
        assertEquals(first.id, detail.id)
        assertEquals(first.body, detail.body)
    }

    @Test
    fun `opening a Floor returns one community, not a list`() {
        val list = json.decodeFromString(CommunityListDto.serializer(), get("communities"))
        val target = list.items.first { it.membershipState == "NOT_JOINED" }
        val detail = json.decodeFromString(CommunityDto.serializer(), get("communities", target.id))
        assertEquals(target.id, detail.id)
        assertEquals(target.name, detail.name)
    }

    @Test
    fun `joining a Floor flips membership and moves the member count`() {
        val before = json.decodeFromString(CommunityListDto.serializer(), get("communities"))
            .items.first { it.membershipState == "NOT_JOINED" }

        val joined = json.decodeFromString(
            MembershipDto.serializer(),
            send("POST", "communities", before.id, "join"),
        )
        assertEquals("JOINED", joined.membershipState)
        assertEquals(before.memberCount + 1, joined.memberCount)

        val left = json.decodeFromString(
            MembershipDto.serializer(),
            send("DELETE", "communities", before.id, "join"),
        )
        assertEquals("NOT_JOINED", left.membershipState)
        assertEquals(before.memberCount, left.memberCount)
    }

    @Test
    fun `posting a discussion returns the new post and it appears in the feed`() {
        val created = json.decodeFromString(
            PostDto.serializer(),
            send("POST", "talk", "posts", body = """{"categoryId":"c2","body":"Does anyone actually read the QA scorecard?"}"""),
        )
        assertEquals("Does anyone actually read the QA scorecard?", created.body)
        assertEquals("c2", created.categoryId)
        assertEquals("Leadership", created.categoryName)

        val feed = json.decodeFromString(PostPageDto.serializer(), get("talk", "posts"))
        assertEquals(created.id, feed.items.first().id)
    }

    @Test
    fun `posting a Pulse returns the new pulse and respects the 220 character ceiling`() {
        val long = "x".repeat(400)
        val created = json.decodeFromString(
            PulseDto.serializer(),
            send("POST", "pulse", body = """{"body":"$long"}"""),
        )
        assertEquals(220, created.body.length)

        val feed = json.decodeFromString(PulsePageDto.serializer(), get("pulse"))
        assertEquals(created.id, feed.items.first().id)
    }

    @Test
    fun `commenting returns the comment and raises the reply count`() {
        val post = json.decodeFromString(PostPageDto.serializer(), get("talk", "posts")).items.first()
        val before = post.commentCount

        val comment = json.decodeFromString(
            CommentDto.serializer(),
            send("POST", "talk", "posts", post.id, "comments", body = """{"body":"Agreed, and the data backs it up."}"""),
        )
        assertEquals("Agreed, and the data backs it up.", comment.body)

        val after = json.decodeFromString(PostDto.serializer(), get("talk", "posts", post.id))
        assertEquals(before + 1, after.commentCount)
    }

    @Test
    fun `the three Invite sub-screens all parse`() {
        val milestones = json.decodeFromString(
            ListSerializer(MilestoneTierDto.serializer()),
            get("referrals", "milestones"),
        )
        assertEquals(listOf(5, 25, 100, 500, 1000), milestones.map { it.thresholdActive })

        val history = json.decodeFromString(ReferralListDto.serializer(), get("referrals"))
        assertTrue(history.items.isNotEmpty())

        val faq = json.decodeFromString(ListSerializer(FaqItemDto.serializer()), get("referrals", "faq"))
        assertTrue(faq.all { it.q.isNotBlank() && it.a.isNotBlank() })
    }

    // ---- the points rules the guide says the server owns ----------------

    @Test
    fun `a discussion awards ten points, and the third one in a day awards nothing`() {
        val start = balance()
        send("POST", "talk", "posts", body = """{"categoryId":"c1","body":"First of the day."}""")
        assertEquals(start + 10, balance())

        send("POST", "talk", "posts", body = """{"categoryId":"c1","body":"Second of the day."}""")
        assertEquals(start + 20, balance())

        // Capped at two a day — the third is posted but pays nothing.
        send("POST", "talk", "posts", body = """{"categoryId":"c1","body":"Third of the day."}""")
        assertEquals(start + 20, balance())
    }

    @Test
    fun `Pulse never touches the ledger`() {
        val start = balance()
        send("POST", "pulse", body = """{"body":"Coffee machine is broken again."}""")
        send("POST", "pulse", body = """{"body":"Night shift, hello."}""")
        assertEquals(start, balance())
    }

    @Test
    fun `an award lands in the ledger and raises a notification`() {
        val before = json.decodeFromString(RewardTransactionsDto.serializer(), get("rewards", "transactions")).items.size
        send("POST", "talk", "posts", body = """{"categoryId":"c3","body":"What does a fair pay band look like?"}""")
        val after = json.decodeFromString(RewardTransactionsDto.serializer(), get("rewards", "transactions")).items
        assertEquals(before + 1, after.size)
        assertEquals(10L, after.first().deltaCredits)
    }

    // ---- profile edits actually stick ----------------------------------

    @Test
    fun `editing the profile changes it and recomputes completeness`() {
        val before = json.decodeFromString(ProfileDto.serializer(), get("users", "me"))
        val updated = json.decodeFromString(
            ProfileDto.serializer(),
            send("PATCH", "users", "me", "profile", body = """{"city":"Cape Town","role":"Team Leader"}"""),
        )
        assertEquals("Cape Town", updated.city)
        assertEquals("Team Leader", updated.role)
        // Untouched fields survive the merge.
        assertEquals(before.employer, updated.employer)
        assertTrue(updated.completeness in 1..100)

        val reread = json.decodeFromString(ProfileDto.serializer(), get("users", "me"))
        assertEquals("Cape Town", reread.city)
    }

    // ---- reads the rest of the app depends on --------------------------

    @Test
    fun `search and the kind filter are applied, not ignored`() {
        val search = json.decodeFromString(
            CommunityListDto.serializer(),
            get("communities", query = { if (it == "query") "philippines" else null }),
        )
        assertEquals(1, search.items.size)
        assertEquals("Philippines Floor", search.items.first().name)

        val shifts = json.decodeFromString(
            CommunityListDto.serializer(),
            get("communities", query = { if (it == "kind") "SHIFT" else null }),
        )
        assertTrue(shifts.items.isNotEmpty())
        assertTrue(shifts.items.all { it.kind == "SHIFT" })
    }

    @Test
    fun `the category filter narrows the Talk feed`() {
        val leadership = json.decodeFromString(
            PostPageDto.serializer(),
            get("talk", "posts", query = { if (it == "category") "c2" else null }),
        )
        assertTrue(leadership.items.isNotEmpty())
        assertTrue(leadership.items.all { it.categoryId == "c2" })
    }

    @Test
    fun `Home reflects the state the rest of the app changed`() {
        val target = json.decodeFromString(CommunityListDto.serializer(), get("communities"))
            .items.first { it.membershipState == "NOT_JOINED" }
        send("POST", "communities", target.id, "join")

        val home = json.decodeFromString(HomeDto.serializer(), get("home"))
        assertTrue(home.myFloors.any { it.id == target.id })
        assertEquals(balance(), home.creditsBalance)
        assertNotNull(home.presenceNames.firstOrNull())
    }

    @Test
    fun `the demo user is who the app thinks it is`() {
        val me = json.decodeFromString(ProfileDto.serializer(), get("users", "me"))
        assertEquals(DemoMode.USER_ID, me.userId)
    }
}
