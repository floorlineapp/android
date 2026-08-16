package com.thefloor.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.feature.auth.ForgotPasswordScreen
import com.thefloor.app.feature.auth.LoginScreen
import com.thefloor.app.feature.auth.OnboardingScreen
import com.thefloor.app.feature.auth.ResetPasswordScreen
import com.thefloor.app.feature.auth.SignUpScreen
import com.thefloor.app.feature.auth.VerifyEmailScreen
import com.thefloor.app.feature.auth.WelcomeScreen
import com.thefloor.app.feature.floor.CommunityDetailScreen
import com.thefloor.app.feature.floor.DiscoverScreen
import com.thefloor.app.feature.home.HomeScreen
import com.thefloor.app.feature.invite.InviteEarnScreen
import com.thefloor.app.feature.invite.InviteFaqScreen
import com.thefloor.app.feature.invite.InviteHistoryScreen
import com.thefloor.app.feature.invite.InviteMilestonesScreen
import com.thefloor.app.feature.more.MoreScreen
import com.thefloor.app.feature.notifications.NotificationCenterScreen
import com.thefloor.app.feature.notifications.NotificationPrefsScreen
import com.thefloor.app.feature.profile.EditProfileScreen
import com.thefloor.app.feature.profile.PrivacyScreen
import com.thefloor.app.feature.profile.ProfileScreen
import com.thefloor.app.feature.radio.RadioScreen
import com.thefloor.app.feature.rewards.RewardTransactionsScreen
import com.thefloor.app.feature.rewards.RewardsScreen
import com.thefloor.app.feature.settings.DeleteAccountScreen
import com.thefloor.app.feature.settings.SettingsScreen
import com.thefloor.app.feature.talk.ComposePostScreen
import com.thefloor.app.feature.talk.PostDetailScreen
import com.thefloor.app.feature.talk.TalkFeedScreen

@Composable
fun FloorNavHost(
    navController: NavHostController,
    sessionState: SessionState,
    modifier: Modifier = Modifier,
) {
    val startDestination = when (sessionState) {
        SessionState.LOADING -> Routes.WELCOME // splash covers this frame
        SessionState.SIGNED_OUT -> Routes.WELCOME
        SessionState.SIGNED_IN -> Routes.HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ---------------- auth ----------------
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onSignUp = { navController.navigate(Routes.signUp()) },
                onLogIn = { navController.navigate(Routes.LOG_IN) },
            )
        }
        composable(
            Routes.SIGN_UP,
            arguments = listOf(navArgument("code") { type = NavType.StringType; nullable = true }),
        ) {
            SignUpScreen(
                onSignedUp = { navController.navigate(Routes.VERIFY_EMAIL) { popUpTo(Routes.WELCOME) { inclusive = true } } },
                onLogIn = { navController.navigate(Routes.LOG_IN) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LOG_IN) {
            LoginScreen(
                onLoggedIn = { verified ->
                    val next = if (verified) Routes.HOME else Routes.VERIFY_EMAIL
                    navController.navigate(next) { popUpTo(Routes.WELCOME) { inclusive = true } }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.VERIFY_EMAIL) {
            VerifyEmailScreen(
                onVerified = { navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.VERIFY_EMAIL) { inclusive = true } } },
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.RESET_PASSWORD,
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
        ) { entry ->
            ResetPasswordScreen(
                token = entry.arguments?.getString("token").orEmpty(),
                onDone = { navController.navigate(Routes.LOG_IN) { popUpTo(Routes.WELCOME) } },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = { navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } } },
            )
        }

        // ---------------- main tabs ----------------
        composable(Routes.HOME) {
            HomeScreen(
                onOpenInvite = { navController.navigate(Routes.INVITE_EARN) },
                onOpenRewards = { navController.navigate(Routes.REWARDS) },
                onOpenFloorTab = { navController.navigate(Routes.FLOOR) },
                onOpenCommunity = { navController.navigate(Routes.communityDetail(it)) },
                onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenProfileEdit = { navController.navigate(Routes.PROFILE_EDIT) },
            )
        }
        composable(Routes.FLOOR) {
            DiscoverScreen(onOpenCommunity = { navController.navigate(Routes.communityDetail(it)) })
        }
        composable(Routes.TALK) {
            TalkFeedScreen(
                onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                onCompose = { navController.navigate(Routes.COMPOSE_POST) },
            )
        }
        composable(Routes.RADIO) {
            RadioScreen()
        }
        composable(Routes.MORE) {
            MoreScreen(
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        // ---------------- details ----------------
        composable(
            Routes.COMMUNITY_DETAIL,
            arguments = listOf(navArgument("communityId") { type = NavType.StringType }),
        ) { entry ->
            CommunityDetailScreen(
                communityId = entry.arguments?.getString("communityId").orEmpty(),
                onBack = { navController.popBackStack() },
                onOpenPost = { navController.navigate(Routes.postDetail(it)) },
            )
        }
        composable(
            Routes.POST_DETAIL,
            arguments = listOf(navArgument("postId") { type = NavType.StringType }),
        ) { entry ->
            PostDetailScreen(
                postId = entry.arguments?.getString("postId").orEmpty(),
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.publicProfile(it)) },
            )
        }
        composable(Routes.COMPOSE_POST) {
            ComposePostScreen(onDone = { navController.popBackStack() })
        }

        // ---------------- invite & earn ----------------
        composable(Routes.INVITE_EARN) {
            InviteEarnScreen(
                onBack = { navController.popBackStack() },
                onOpenMilestones = { navController.navigate(Routes.INVITE_MILESTONES) },
                onOpenHistory = { navController.navigate(Routes.INVITE_HISTORY) },
                onOpenFaq = { navController.navigate(Routes.INVITE_FAQ) },
            )
        }
        composable(Routes.INVITE_MILESTONES) {
            InviteMilestonesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.INVITE_HISTORY) {
            InviteHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.INVITE_FAQ) {
            InviteFaqScreen(onBack = { navController.popBackStack() })
        }

        // ---------------- rewards / notifications ----------------
        composable(Routes.REWARDS) {
            RewardsScreen(
                onBack = { navController.popBackStack() },
                onOpenTransactions = { navController.navigate(Routes.REWARD_TRANSACTIONS) },
                onOpenInvite = { navController.navigate(Routes.INVITE_EARN) },
            )
        }
        composable(Routes.REWARD_TRANSACTIONS) {
            RewardTransactionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationCenterScreen(
                onBack = { navController.popBackStack() },
                onOpenPrefs = { navController.navigate(Routes.NOTIFICATION_PREFS) },
                onOpenDeepLink = { uri ->
                    when (val target = com.thefloor.app.domain.DeepLinkParser.parse(uri)) {
                        is com.thefloor.app.domain.DeepLinkParser.Target.Invite,
                        com.thefloor.app.domain.DeepLinkParser.Target.InviteEarn ->
                            navController.navigate(Routes.INVITE_EARN)
                        is com.thefloor.app.domain.DeepLinkParser.Target.TalkPost ->
                            navController.navigate(Routes.postDetail(target.postId))
                        is com.thefloor.app.domain.DeepLinkParser.Target.Floor ->
                            navController.navigate(Routes.communityDetail(target.communityId))
                        com.thefloor.app.domain.DeepLinkParser.Target.Rewards ->
                            navController.navigate(Routes.REWARDS)
                        else -> Unit
                    }
                },
            )
        }
        composable(Routes.NOTIFICATION_PREFS) {
            NotificationPrefsScreen(onBack = { navController.popBackStack() })
        }

        // ---------------- profile / settings ----------------
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.PROFILE_EDIT) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
            )
        }
        composable(Routes.PROFILE_EDIT) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.PUBLIC_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { entry ->
            com.thefloor.app.feature.profile.PublicProfileScreen(
                userId = entry.arguments?.getString("userId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNotificationPrefs = { navController.navigate(Routes.NOTIFICATION_PREFS) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
                onDeleteAccount = { navController.navigate(Routes.DELETE_ACCOUNT) },
            )
        }
        composable(Routes.DELETE_ACCOUNT) {
            DeleteAccountScreen(onBack = { navController.popBackStack() })
        }

        // ---------------- phase-2 stubs (flag-gated; deep links never dead-end) ----------------
        composable(Routes.JOBS) { ComingSoon("Employers & Jobs", "Browse and apply to roles from verified employers — coming soon.") }
        composable(Routes.ACADEMY) { ComingSoon("Academy", "Courses that move you from Agent to Director — coming soon.") }
        composable(Routes.MARKETPLACE) { ComingSoon("Marketplace", "Partner deals for the people on The Floor — coming soon.") }
        composable(Routes.INSIGHTS) { ComingSoon("Workplace Insights", "Anonymous, aggregated workplace insight — coming soon.") }
        composable(Routes.ABOUT) {
            ComingSoon("About The Floor", "The global home of the people behind every customer conversation.")
        }
    }
}

@Composable
private fun ComingSoon(title: String, message: String) {
    FloorEmptyState(title = title, message = message)
}
