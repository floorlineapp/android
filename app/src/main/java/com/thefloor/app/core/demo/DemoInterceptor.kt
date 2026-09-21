package com.thefloor.app.core.demo

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Answers every API call locally when [DemoMode] is on, so the app is fully
 * explorable with no backend. Sits first in the chain, so nothing ever reaches
 * the network — which also means a demo build works offline apart from the
 * remote cover photos.
 *
 * Responses are the real DTO shapes; everything downstream (mapping, caching,
 * ViewModels, UI) runs exactly as it does against the live server.
 */
@Singleton
class DemoInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!DemoMode.enabled) return chain.proceed(request)

        val path = request.url.encodedPath.removePrefix("/").removeSuffix("/")
        val body = bodyFor(path, request.method)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (demo)")
            .body(body.toResponseBody(JSON))
            .build()
    }

    private fun bodyFor(path: String, method: String): String = when {
        path.endsWith("auth/login") || path.endsWith("auth/signup") -> AUTH
        path.endsWith("auth/refresh") -> TOKENS
        path.endsWith("config") -> CONFIG
        path.endsWith("home") -> home()
        path.endsWith("users/me") -> PROFILE
        path.contains("users/") && method == "GET" -> PROFILE
        path.endsWith("communities") || path.endsWith("communities/suggested") -> COMMUNITIES
        path.endsWith("talk/categories") -> CATEGORIES
        path.contains("talk/posts") && path.endsWith("comments") -> COMMENTS
        path.contains("talk/posts") -> posts()
        path.endsWith("pulse") -> pulses()
        path.endsWith("rewards/summary") -> REWARDS
        path.endsWith("rewards/transactions") -> TRANSACTIONS
        path.endsWith("referrals/summary") -> REFERRALS
        path.endsWith("notifications") -> NOTIFICATIONS
        path.endsWith("notifications/preferences") -> "{}"
        // Writes and anything unmapped: benign, and shaped so any list DTO
        // still deserializes.
        else -> EMPTY
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        const val EMPTY = """{"ok":true,"items":[]}"""

        /** ISO-8601 timestamp [hoursAgo] in the past, so "2h ago" stays sensible. */
        fun ago(hoursAgo: Long): String =
            Instant.now().minus(hoursAgo, ChronoUnit.HOURS).toString()

        const val TOKENS =
            """{"accessToken":"demo.access.token","refreshToken":"demo.refresh.token","expiresInSeconds":86400}"""

        val AUTH = """{"userId":"${DemoMode.USER_ID}","emailVerified":true,"tokens":$TOKENS}"""

        const val CONFIG = """{
          "flags":{"radio":true,"academy":true,"jobs":true,"insights":true,"marketplace":true,"payouts":true},
          "minAppVersion":1,
          "disclosures":["Demo build — all data on this device is sample content.",
                         "Free to refer. Nobody pays to unlock earning."],
          "legalUrls":{"terms":"https://thefloor.app/legal/terms","privacy":"https://thefloor.app/legal/privacy"}
        }"""

        val PROFILE = """{
          "userId":"${DemoMode.USER_ID}","displayName":"Naledi M.","country":"South Africa","city":"Johannesburg",
          "ageRange":"25-34","languages":["English","Zulu"],
          "employer":"Meridian Contact Solutions","site":"Rosebank Contact Center",
          "industry":"Telecom","role":"Senior Agent","careerLevel":"SENIOR_AGENT",
          "experienceYears":4,"channels":["Voice","Chat"],"workMode":"HYBRID",
          "skills":["De-escalation","Billing systems","CRM","Coaching new hires"],
          "completeness":78,"emailVerified":true,
          "visibility":{"employer":"MEMBERS","city":"PUBLIC","country":"PUBLIC","role":"PUBLIC"}
        }"""

        private const val U = "https://images.unsplash.com/"
        private const val P = "?w=1200&q=80&auto=format&fit=crop"

        val COMMUNITIES = """{"items":[
          ${community("za", "South Africa Floor", "From Cape Town to Joburg, Durban to everywhere in between.", "COUNTRY", 23461, "JOINED", "photo-1602578984228-c98a9b995f3e")},
          ${community("ph", "Philippines Floor", "The heart of global outsourcing. Always on, always awake.", "COUNTRY", 41208, "NOT_JOINED", "photo-1623518761090-089a985ab17f")},
          ${community("co", "Colombia Floor", "Passion. Resilience. World-class customer experience.", "COUNTRY", 17894, "NOT_JOINED", "photo-1681145553138-816eb004b846")},
          ${community("in", "India Floor", "Talent. Technology. Tomorrow starts here.", "COUNTRY", 142388, "NOT_JOINED", "photo-1545562083-c583d014b4f2")},
          ${community("mx", "Mexico Floor", "Bridging cultures. Delivering excellence.", "COUNTRY", 15662, "NOT_JOINED", "photo-1591049433264-618fa2f4558f")},
          ${community("eg", "Egypt Floor", "Connecting the world from the heart of Egypt.", "COUNTRY", 12807, "NOT_JOINED", "photo-1568322445389-f64ac2515020")},
          ${community("pl", "Poland Floor", "European quality. Global impact.", "COUNTRY", 8945, "NOT_JOINED", "photo-1651062108412-36a68f3748dd")},
          ${community("br", "Brazil Floor", "Empathy. Energy. Extraordinary customer connections.", "COUNTRY", 19774, "NOT_JOINED", "photo-1483729558449-99ef09a8c325")},
          ${community("ke", "Kenya Floor", "Africa's rising delivery hub.", "COUNTRY", 6410, "NOT_JOINED", "photo-1611348586804-61bf6c080437")},
          ${community("us", "United States Floor", "Innovation. Service. Leadership.", "COUNTRY", 31589, "NOT_JOINED", "photo-1485738422979-f5c462d49f74")},
          ${community("rm", "Remote Floor", "Work from anywhere. Belong everywhere.", "GLOBAL", 21270, "JOINED", "photo-1616531770192-6eaea74c2456")},
          ${community("ns", "Night Shift Warriors", "For the people keeping the world awake.", "SHIFT", 9328, "NOT_JOINED", "photo-1544202482-5e970f02d8e7")},
          ${community("na", "New Agents", "First week on the phones? Start here.", "CAREER_LEVEL", 12044, "NOT_JOINED", "photo-1600880292203-757bb62b4baf")},
          ${community("tl", "Team Leaders", "Coaching, metrics and leading a floor of your own.", "CAREER_LEVEL", 7781, "NOT_JOINED", "photo-1542744173-8e7e53415bb0")},
          ${community("gl", "The Floor — Global", "The global home of the people behind every customer conversation.", "GLOBAL", 128400, "JOINED", "photo-1521737604893-d14cc237f11d")}
        ]}"""

        fun community(
            id: String, name: String, desc: String, kind: String,
            members: Int, state: String, photo: String,
        ) = """{"id":"$id","slug":"$id","name":"$name","description":"$desc","kind":"$kind",
                "memberCount":$members,"isRestricted":false,"membershipState":"$state",
                "imageUrl":"$U$photo$P"}"""

        const val CATEGORIES = """{"items":[
          {"id":"c1","slug":"the-job","name":"The Job"},
          {"id":"c2","slug":"leadership","name":"Leadership"},
          {"id":"c3","slug":"pay","name":"Pay & Progression"},
          {"id":"c4","slug":"ai","name":"AI & The Future"},
          {"id":"c5","slug":"big","name":"The Big Questions"}
        ]}"""

        fun post(
            id: String, author: String, level: String, cat: String, catName: String,
            body: String, comments: Int, reactions: Int, hours: Long,
        ) = """{"id":"$id","authorId":"u$id","authorName":"$author","authorLevel":"$level",
                "categoryId":"$cat","categoryName":"$catName","communityId":"za","body":"$body",
                "commentCount":$comments,"reactionCount":$reactions,"saved":false,"createdAt":"${ago(hours)}"}"""

        fun posts() = """{"items":[
          ${post("p1", "Mika R.", "TEAM_LEADER", "c1", "The Job", "Should agents be penalised for AHT when the customer genuinely needs more time?", 118, 296, 3)},
          ${post("p2", "Thabo N.", "SENIOR_AGENT", "c2", "Leadership", "When does coaching become micromanagement? Where should a Team Leader draw the line?", 76, 184, 8)},
          ${post("p3", "Priya S.", "SME", "c3", "Pay & Progression", "Are BPO salaries keeping pace with what companies now expect agents to handle?", 129, 341, 14)},
          ${post("p4", "Owen K.", "AGENT", "c4", "AI & The Future", "AI quality scoring is here. Should an algorithm be allowed to affect an agent bonus?", 143, 267, 22)},
          ${post("p5", "Grace A.", "AGENT", "c3", "Pay & Progression", "What should a real career path from Agent to Team Leader actually look like?", 82, 219, 30)},
          ${post("p6", "Meridian Contact Solutions", "MANAGER", "c5", "The Big Questions", "Is the BPO industry ready to give frontline employees a stronger voice in how operations are designed?", 105, 198, 46)}
        ],"nextCursor":null}"""

        const val COMMENTS = """{"items":[
          {"id":"cm1","authorId":"u9","authorName":"Sipho D.","body":"We measure AHT but never measure whether the problem actually got solved. That is the real gap.","createdAt":"2026-09-19T09:12:00Z"},
          {"id":"cm2","authorId":"u4","authorName":"Carmen V.","body":"My TL protects us from the stopwatch and our CSAT is the highest on site. It can be done.","createdAt":"2026-09-19T11:40:00Z"}
        ],"nextCursor":null}"""

        fun pulses() = """{"items":[
          {"id":"x1","authorId":"u2","authorName":"Thabo N.","body":"Third escalation before 9am and the coffee machine is broken. Send help.","likeCount":24,"liked":false,"createdAt":"${ago(2)}"},
          {"id":"x2","authorId":"${DemoMode.USER_ID}","authorName":"Naledi M.","body":"Just closed the longest call of my life. 74 minutes. We got there.","likeCount":61,"liked":true,"createdAt":"${ago(5)}"},
          {"id":"x3","authorId":"u3","authorName":"Grace A.","body":"Night shift crew — what are we listening to tonight?","likeCount":12,"liked":false,"createdAt":"${ago(9)}"},
          {"id":"x4","authorId":"u7","authorName":"Owen K.","body":"Passed my QA review with 96%. Six months ago I was at 71%.","likeCount":88,"liked":false,"createdAt":"${ago(20)}"}
        ],"nextCursor":null}"""

        // The seven earning actions from the build spec, with their caps stated
        // in the label. Invite & Grow is deliberately absent: referrals never
        // add Floor Points.
        const val REWARDS = """{"creditsBalance":12480,"waysToEarn":[
          {"title":"Complete your verified profile — once","credits":50,"deepLink":"thefloor://profile/edit"},
          {"title":"Start a Talk discussion — up to 2 a day","credits":10,"deepLink":"thefloor://talk"},
          {"title":"Give an answer marked Helpful — capped daily","credits":25,"deepLink":"thefloor://talk"},
          {"title":"Floor-verified Academy learning — per item","credits":25,"deepLink":"thefloor://academy"},
          {"title":"Attend a verified Floor event — per event","credits":50,"deepLink":"thefloor://events"},
          {"title":"Workplace Spotlight approved — per story","credits":75,"deepLink":"thefloor://insights"},
          {"title":"Win an official game or competition","credits":150,"deepLink":"thefloor://rewards"}
        ],"recentTransactions":[]}"""

        val TRANSACTIONS = """{"items":[
          {"id":"t1","deltaCredits":150,"reason":"Friday Trivia winner","createdAt":"${ago(48)}"},
          {"id":"t2","deltaCredits":-155,"reason":"Reward redemption","createdAt":"${ago(72)}"},
          {"id":"t3","deltaCredits":75,"reason":"Approved Workplace Spotlight","createdAt":"${ago(96)}"},
          {"id":"t4","deltaCredits":10,"reason":"Meaningful discussion posted","createdAt":"${ago(120)}"},
          {"id":"t5","deltaCredits":25,"reason":"Floor-verified learning: Soft Skills","createdAt":"${ago(144)}"},
          {"id":"t6","deltaCredits":50,"reason":"Verified profile completed","createdAt":"${ago(200)}"}
        ]}"""

        const val REFERRALS = """{
          "code":"FLOORDEMO","link":"https://thefloor.app/invite/FLOORDEMO",
          "invited":12,"active":5,"credits":500,"cashEarnedMinor":2500,"cashCurrency":"EUR",
          "ambassadorStatus":"CONNECTOR",
          "nextMilestone":{"thresholdActive":10,"currentActive":5,"remaining":5,"progressPct":50,
            "rewards":[{"kind":"CREDITS","credits":500},{"kind":"CASH","amountMinor":5000,"currency":"EUR"}]},
          "disclosures":["Free to refer. Nobody pays to unlock earning.",
                         "Cash bonuses are funded by The Floor's own revenue."]
        }"""

        val NOTIFICATIONS = """{"items":[
          {"id":"n1","type":"TALK","title":"Mika R. replied to your discussion","body":"\"That is exactly the point everyone misses.\"","read":false,"createdAt":"${ago(1)}"},
          {"id":"n2","type":"REWARD","title":"You earned 50 Floor Points","body":"Verified profile completed.","read":false,"createdAt":"${ago(26)}"},
          {"id":"n3","type":"FLOOR","title":"Welcome to the South Africa Floor","body":"23,461 people are already here.","read":true,"createdAt":"${ago(50)}"}
        ]}"""

        fun home() = """{
          "displayName":"Naledi M.","presenceCount":2347,
          "completionCards":[{"title":"Complete your profile","subtitle":"Add your workplace to see who else works there.","deepLink":"thefloor://profile/edit"}],
          "myFloors":[
            ${community("za", "South Africa Floor", "From Cape Town to Joburg, Durban to everywhere in between.", "COUNTRY", 23461, "JOINED", "photo-1602578984228-c98a9b995f3e")},
            ${community("gl", "The Floor — Global", "The global home of the people behind every customer conversation.", "GLOBAL", 128400, "JOINED", "photo-1521737604893-d14cc237f11d")}
          ],
          "trendingPosts":[
            ${post("p1", "Mika R.", "TEAM_LEADER", "c1", "The Job", "Should agents be penalised for AHT when the customer genuinely needs more time?", 118, 296, 3)},
            ${post("p2", "Thabo N.", "SENIOR_AGENT", "c2", "Leadership", "When does coaching become micromanagement? Where should a Team Leader draw the line?", 76, 184, 8)},
            ${post("p3", "Priya S.", "SME", "c3", "Pay & Progression", "Are BPO salaries keeping pace with what companies now expect agents to handle?", 129, 341, 14)}
          ],
          "inviteEarn":{"invited":12,"active":5},
          "creditsBalance":12480,
          "flags":{"radio":true,"academy":true,"jobs":true,"insights":true,"marketplace":true}
        }"""
    }
}
