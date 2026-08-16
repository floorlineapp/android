package com.thefloor.app.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** The complete v1 client surface. */
interface FloorApi {

    // ---- public ----
    @GET("v1/config")
    suspend fun config(): PublicConfigDto

    @GET("v1/referrals/resolve/{code}")
    suspend fun resolveReferralCode(@Path("code") code: String): ResolveDto

    // ---- auth ----
    @POST("v1/auth/signup")
    suspend fun signup(@Body body: SignupRequestDto): AuthResponseDto

    @POST("v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): TokenPairDto

    @POST("v1/auth/logout")
    suspend fun logout(@Body body: RefreshRequestDto): OkDto

    @POST("v1/auth/verify/confirm")
    suspend fun confirmVerification(@Body body: TokenBodyDto): OkDto

    @POST("v1/auth/verify/resend")
    suspend fun resendVerification(): OkDto

    @POST("v1/auth/password/forgot")
    suspend fun forgotPassword(@Body body: EmailBodyDto): OkDto

    @POST("v1/auth/password/reset")
    suspend fun resetPassword(@Body body: ResetRequestDto): OkDto

    // ---- home / profile ----
    @GET("v1/home")
    suspend fun home(): HomeDto

    @GET("v1/users/me")
    suspend fun me(): ProfileDto

    @PATCH("v1/users/me/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): ProfileDto

    @PUT("v1/users/me/privacy")
    suspend fun updatePrivacy(@Body body: PrivacyRequestDto): ProfileDto

    @GET("v1/users/{id}")
    suspend fun publicProfile(@Path("id") userId: String): ProfileDto

    @POST("v1/users/{id}/block")
    suspend fun blockUser(@Path("id") userId: String): OkDto

    @DELETE("v1/users/{id}/block")
    suspend fun unblockUser(@Path("id") userId: String): OkDto

    @retrofit2.http.HTTP(method = "DELETE", path = "v1/users/me", hasBody = true)
    suspend fun deleteAccount(@Body body: DeleteAccountRequestDto): OkDto

    // ---- communities ----
    @GET("v1/communities")
    suspend fun communities(
        @Query("query") query: String? = null,
        @Query("kind") kind: String? = null,
    ): CommunityListDto

    @GET("v1/communities/suggested")
    suspend fun suggestedCommunities(): CommunityListDto

    @GET("v1/communities/{id}")
    suspend fun community(@Path("id") id: String): CommunityDto

    @POST("v1/communities/{id}/join")
    suspend fun joinCommunity(@Path("id") id: String): MembershipDto

    @DELETE("v1/communities/{id}/join")
    suspend fun leaveCommunity(@Path("id") id: String): MembershipDto

    // ---- talk ----
    @GET("v1/talk/categories")
    suspend fun talkCategories(): CategoriesDto

    @GET("v1/talk/posts")
    suspend fun posts(
        @Query("category") categoryId: String? = null,
        @Query("community") communityId: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): PostPageDto

    @GET("v1/talk/posts/{id}")
    suspend fun post(@Path("id") id: String): PostDto

    @POST("v1/talk/posts")
    suspend fun createPost(@Body body: CreatePostRequestDto): PostDto

    @GET("v1/talk/posts/{id}/comments")
    suspend fun comments(@Path("id") postId: String, @Query("cursor") cursor: String? = null): CommentPageDto

    @POST("v1/talk/posts/{id}/comments")
    suspend fun createComment(@Path("id") postId: String, @Body body: CreateCommentRequestDto): CommentDto

    @PUT("v1/talk/posts/{id}/reaction")
    suspend fun setReaction(@Path("id") postId: String, @Body body: ReactionRequestDto)

    @DELETE("v1/talk/posts/{id}/reaction")
    suspend fun clearReaction(@Path("id") postId: String)

    @POST("v1/talk/posts/{id}/save")
    suspend fun savePost(@Path("id") postId: String)

    @DELETE("v1/talk/posts/{id}/save")
    suspend fun unsavePost(@Path("id") postId: String)

    @POST("v1/reports")
    suspend fun report(@Body body: ReportRequestDto): OkDto

    // ---- referrals ----
    @GET("v1/referrals/summary")
    suspend fun referralSummary(): ReferralSummaryDto

    @GET("v1/referrals/milestones")
    suspend fun referralMilestones(): List<MilestoneTierDto>

    @GET("v1/referrals")
    suspend fun referralHistory(): ReferralListDto

    @GET("v1/referrals/faq")
    suspend fun referralFaq(): List<FaqItemDto>

    @POST("v1/referrals/link/share-event")
    suspend fun shareEvent(@Body body: ShareEventDto)

    // ---- rewards ----
    @GET("v1/rewards/summary")
    suspend fun rewardsSummary(): RewardsSummaryDto

    @GET("v1/rewards/transactions")
    suspend fun rewardTransactions(): RewardTransactionsDto

    // ---- notifications ----
    @GET("v1/notifications")
    suspend fun notifications(): NotificationsDto

    @POST("v1/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): OkDto

    @POST("v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): OkDto

    @GET("v1/notifications/preferences")
    suspend fun notificationPrefs(): PrefsDto

    @PUT("v1/notifications/preferences")
    suspend fun updateNotificationPrefs(@Body body: PrefsDto): PrefsDto
}
