package com.thefloor.app.core.data

import com.thefloor.app.core.common.AppResult
import com.thefloor.app.core.common.map
import com.thefloor.app.core.common.onSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.thefloor.app.core.database.CachedCommunityEntity
import com.thefloor.app.core.database.CachedNotificationEntity
import com.thefloor.app.core.database.CachedPostEntity
import com.thefloor.app.core.database.CommunityDao
import com.thefloor.app.core.database.NotificationDao
import com.thefloor.app.core.database.PostDao
import com.thefloor.app.core.model.AppNotification
import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.Comment
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.CompletionCard
import com.thefloor.app.core.model.FaqItem
import com.thefloor.app.core.model.HomeContent
import com.thefloor.app.core.model.MembershipState
import com.thefloor.app.core.model.MilestoneTier
import com.thefloor.app.core.model.Post
import com.thefloor.app.core.model.Pulse
import com.thefloor.app.core.model.ReferralHistoryItem
import com.thefloor.app.core.model.ReferralSummary
import com.thefloor.app.core.model.RewardTransaction
import com.thefloor.app.core.model.RewardsSummary
import com.thefloor.app.core.model.TalkCategory
import com.thefloor.app.core.model.UserProfile
import com.thefloor.app.core.model.WayToEarn
import com.thefloor.app.core.network.CreateCommentRequestDto
import com.thefloor.app.core.network.CreatePostRequestDto
import com.thefloor.app.core.network.DeleteAccountRequestDto
import com.thefloor.app.core.network.FloorApi
import com.thefloor.app.core.network.PrefsDto
import com.thefloor.app.core.network.PrivacyRequestDto
import com.thefloor.app.core.network.ReactionRequestDto
import com.thefloor.app.core.network.ReportRequestDto
import com.thefloor.app.core.network.ShareEventDto
import com.thefloor.app.core.network.UpdateProfileRequestDto
import com.thefloor.app.core.network.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: FloorApi) {

    suspend fun me(): AppResult<UserProfile> = safeCall { api.me() }.map { it.toDomain() }

    suspend fun updateProfile(update: UpdateProfileRequestDto): AppResult<UserProfile> =
        safeCall { api.updateProfile(update) }.map { it.toDomain() }

    suspend fun updatePrivacy(visibility: Map<String, String>): AppResult<UserProfile> =
        safeCall { api.updatePrivacy(PrivacyRequestDto(visibility)) }.map { it.toDomain() }

    suspend fun publicProfile(userId: String): AppResult<UserProfile> =
        safeCall { api.publicProfile(userId) }.map { it.toDomain() }

    suspend fun blockUser(userId: String): AppResult<Unit> =
        safeCall { api.blockUser(userId) }.map { }

    suspend fun deleteAccount(password: String): AppResult<Unit> =
        safeCall { api.deleteAccount(DeleteAccountRequestDto(password)) }.map { }
}

@Singleton
class HomeRepository @Inject constructor(private val api: FloorApi) {

    suspend fun load(): AppResult<HomeContent> = safeCall { api.home() }.map { dto ->
        HomeContent(
            displayName = dto.displayName,
            presenceCount = dto.presenceCount,
            completionCards = dto.completionCards.map { CompletionCard(it.title, it.subtitle, it.deepLink) },
            myFloors = dto.myFloors.map { it.toDomain() },
            trendingPosts = dto.trendingPosts.map { it.toDomain() },
            invitedCount = dto.inviteEarn.invited,
            activeReferrals = dto.inviteEarn.active,
            creditsBalance = dto.creditsBalance,
            flags = dto.flags,
        )
    }
}

@Singleton
class CommunityRepository @Inject constructor(
    private val api: FloorApi,
    private val dao: CommunityDao,
) {
    /** Cached floors for offline rendering. */
    val cached: Flow<List<Community>> = dao.observeAll().map { list ->
        list.map {
            Community(
                it.id, it.slug, it.name, it.description, it.kind, it.memberCount,
                it.isRestricted,
                runCatching { MembershipState.valueOf(it.membershipState) }
                    .getOrDefault(MembershipState.NOT_JOINED),
            )
        }
    }

    suspend fun discover(query: String?, kind: String?): AppResult<List<Community>> =
        safeCall { api.communities(query = query?.ifBlank { null }, kind = kind) }
            .map { it.items.map { dto -> dto.toDomain() } }
            .onSuccess { communities ->
                val now = System.currentTimeMillis()
                dao.upsertAll(communities.map {
                    CachedCommunityEntity(
                        it.id, it.slug, it.name, it.description, it.kind,
                        it.memberCount, it.isRestricted, it.membershipState.name, now,
                    )
                })
            }

    suspend fun suggested(): AppResult<List<Community>> =
        safeCall { api.suggestedCommunities() }.map { it.items.map { dto -> dto.toDomain() } }

    suspend fun get(id: String): AppResult<Community> =
        safeCall { api.community(id) }.map { it.toDomain() }

    suspend fun join(id: String): AppResult<MembershipState> =
        safeCall { api.joinCommunity(id) }.map {
            runCatching { MembershipState.valueOf(it.membershipState) }.getOrDefault(MembershipState.JOINED)
        }

    suspend fun leave(id: String): AppResult<Unit> =
        safeCall { api.leaveCommunity(id) }.map { }
}

@Singleton
class TalkRepository @Inject constructor(
    private val api: FloorApi,
    private val dao: PostDao,
) {
    val cachedFeed: Flow<List<Post>> = dao.observeFeed().map { list ->
        list.map {
            Post(
                it.id, it.authorId, it.authorName,
                it.authorLevel?.let { l -> runCatching { CareerLevel.valueOf(l) }.getOrNull() },
                it.categoryId, it.categoryName, it.communityId, it.body,
                it.commentCount, it.reactionCount, it.myReaction, it.saved, it.createdAt,
            )
        }
    }

    suspend fun categories(): AppResult<List<TalkCategory>> =
        safeCall { api.talkCategories() }.map { it.items.map { c -> TalkCategory(c.id, c.slug, c.name) } }

    suspend fun feed(categoryId: String?, cursor: String?, communityId: String? = null): AppResult<Pair<List<Post>, String?>> =
        safeCall { api.posts(categoryId = categoryId, communityId = communityId, cursor = cursor) }
            .map { page -> page.items.map { it.toDomain() } to page.nextCursor }
            .onSuccess { (posts, _) ->
                if (cursor == null && categoryId == null && communityId == null) {
                    val now = System.currentTimeMillis()
                    dao.upsertAll(posts.map {
                        CachedPostEntity(
                            it.id, it.authorId, it.authorName, it.authorLevel?.name,
                            it.categoryId, it.categoryName, it.communityId, it.body,
                            it.commentCount, it.reactionCount, it.myReaction, it.saved,
                            it.createdAt, now,
                        )
                    })
                }
            }

    suspend fun post(id: String): AppResult<Post> = safeCall { api.post(id) }.map { it.toDomain() }

    suspend fun createPost(categoryId: String, body: String, communityId: String?): AppResult<Post> =
        safeCall { api.createPost(CreatePostRequestDto(categoryId, body, communityId)) }.map { it.toDomain() }

    suspend fun comments(postId: String): AppResult<List<Comment>> =
        safeCall { api.comments(postId) }.map { page ->
            page.items.map { Comment(it.id, it.authorId, it.authorName, it.parentId, it.body, it.createdAt) }
        }

    suspend fun addComment(
        postId: String,
        body: String,
        parentId: String? = null,
        mentionUserIds: List<String> = emptyList(),
    ): AppResult<Comment> =
        safeCall { api.createComment(postId, CreateCommentRequestDto(body, parentId, mentionUserIds)) }
            .map { Comment(it.id, it.authorId, it.authorName, it.parentId, it.body, it.createdAt) }

    suspend fun deletePost(postId: String): AppResult<Unit> =
        safeCall { api.deletePost(postId) }.map { }

    suspend fun deleteComment(commentId: String): AppResult<Unit> =
        safeCall { api.deleteComment(commentId) }.map { }

    suspend fun setReaction(postId: String, kind: String): AppResult<Unit> =
        safeCall { api.setReaction(postId, ReactionRequestDto(kind)) }.map { }

    suspend fun clearReaction(postId: String): AppResult<Unit> =
        safeCall { api.clearReaction(postId) }.map { }

    suspend fun toggleSave(postId: String, save: Boolean): AppResult<Unit> =
        safeCall { if (save) api.savePost(postId) else api.unsavePost(postId) }.map { }

    suspend fun report(subjectType: String, subjectId: String, reason: String, detail: String?): AppResult<Unit> =
        safeCall { api.report(ReportRequestDto(subjectType, subjectId, reason, detail)) }.map { }
}

@Singleton
class ReferralRepository @Inject constructor(private val api: FloorApi) {

    suspend fun summary(): AppResult<ReferralSummary> =
        safeCall { api.referralSummary() }.map { it.toDomain() }

    suspend fun milestones(): AppResult<List<MilestoneTier>> =
        safeCall { api.referralMilestones() }.map { list -> list.map { it.toDomain() } }

    suspend fun history(): AppResult<List<ReferralHistoryItem>> =
        safeCall { api.referralHistory() }.map { dto ->
            dto.items.map { ReferralHistoryItem(it.id, it.displayStatus, it.createdAt) }
        }

    suspend fun faq(): AppResult<List<FaqItem>> =
        safeCall { api.referralFaq() }.map { list -> list.map { FaqItem(it.q, it.a) } }

    /** Analytics only; sharing itself goes through the system share sheet. */
    suspend fun recordShare(channel: String) {
        safeCall { api.shareEvent(ShareEventDto(channel)) }
    }
}

@Singleton
class RewardsRepository @Inject constructor(private val api: FloorApi) {

    suspend fun summary(): AppResult<RewardsSummary> =
        safeCall { api.rewardsSummary() }.map { dto ->
            RewardsSummary(
                creditsBalance = dto.creditsBalance,
                waysToEarn = dto.waysToEarn.map { WayToEarn(it.title, it.credits, it.deepLink) },
                recentTransactions = dto.recentTransactions.map {
                    RewardTransaction(it.id, it.deltaCredits, it.reason, it.createdAt)
                },
            )
        }

    suspend fun transactions(): AppResult<List<RewardTransaction>> =
        safeCall { api.rewardTransactions() }.map { dto ->
            dto.items.map { RewardTransaction(it.id, it.deltaCredits, it.reason, it.createdAt) }
        }
}

@Singleton
class NotificationRepository @Inject constructor(
    private val api: FloorApi,
    private val dao: NotificationDao,
) {
    val cached: Flow<List<AppNotification>> = dao.observeAll().map { list ->
        list.map { AppNotification(it.id, it.type, it.title, it.body, it.deepLink, it.read, it.createdAt) }
    }

    suspend fun fetch(): AppResult<Pair<List<AppNotification>, Int>> =
        safeCall { api.notifications() }
            .map { dto ->
                dto.items.map {
                    AppNotification(it.id, it.type, it.title, it.body, it.deepLink, it.read, it.createdAt)
                } to dto.unreadCount
            }
            .onSuccess { (items, _) ->
                val now = System.currentTimeMillis()
                dao.upsertAll(items.map {
                    CachedNotificationEntity(it.id, it.type, it.title, it.body, it.deepLink, it.read, it.createdAt, now)
                })
            }

    suspend fun markRead(id: String): AppResult<Unit> =
        safeCall { api.markNotificationRead(id) }.map { }

    suspend fun markAllRead(): AppResult<Unit> =
        safeCall { api.markAllNotificationsRead() }.map { }

    suspend fun prefs(): AppResult<PrefsDto> = safeCall { api.notificationPrefs() }

    suspend fun updatePrefs(prefs: PrefsDto): AppResult<PrefsDto> =
        safeCall { api.updateNotificationPrefs(prefs) }
}

@Singleton
class ConfigRepository @Inject constructor(private val api: FloorApi) {

    private val flagsMutex = Mutex()
    @Volatile private var cachedFlags: Map<String, Boolean> = emptyMap()

    suspend fun flags(): Map<String, Boolean> {
        cachedFlags.takeIf { it.isNotEmpty() }?.let { return it }
        return flagsMutex.withLock {
            if (cachedFlags.isEmpty()) {
                safeCall { api.config() }.onSuccess { cachedFlags = it.flags }
            }
            cachedFlags
        }
    }

    suspend fun resolveReferral(code: String): AppResult<Pair<Boolean, String?>> =
        safeCall { api.resolveReferralCode(code) }.map { it.valid to it.inviterFirstName }
}

@Singleton
class PulseRepository @Inject constructor(private val api: FloorApi) {

    suspend fun feed(cursor: String? = null): AppResult<Pair<List<Pulse>, String?>> =
        safeCall { api.pulseFeed(cursor) }.map { page -> page.items.map { it.toDomain() } to page.nextCursor }

    suspend fun create(body: String): AppResult<Pulse> =
        safeCall { api.createPulse(com.thefloor.app.core.network.CreatePulseRequestDto(body)) }.map { it.toDomain() }

    suspend fun delete(id: String): AppResult<Unit> = safeCall { api.deletePulse(id) }.map { }

    suspend fun like(id: String): AppResult<Unit> = safeCall { api.likePulse(id) }.map { }

    suspend fun unlike(id: String): AppResult<Unit> = safeCall { api.unlikePulse(id) }.map { }
}
