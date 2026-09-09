package com.thefloor.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.thefloor.app.feature.pulse.PulseScreen
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
    // Static start destination: a dynamic one rebuilds the graph on every
    // session transition and races in-flight auth navigation (QA finding H1).
    // FloorApp's session effect routes WELCOME→HOME for restored sessions.
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME,
        modifier = modifier,
        // Native push/pop feel: content slides and cross-fades between screens.
        enterTransition = {
            fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220))
        },
        exitTransition = {
            fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(180))
        },
        popEnterTransition = {
            fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
        },
        popExitTransition = {
            fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(180))
        },
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
                onSignedUp = { navController.navigate(Routes.verifyEmail()) { popUpTo(Routes.WELCOME) { inclusive = true } } },
                onLogIn = { navController.navigate(Routes.LOG_IN) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LOG_IN) {
            LoginScreen(
                onLoggedIn = { verified ->
                    val next = if (verified) Routes.HOME else Routes.verifyEmail()
                    navController.navigate(next) { popUpTo(Routes.WELCOME) { inclusive = true } }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            Routes.VERIFY_EMAIL,
            arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true }),
        ) {
            VerifyEmailScreen(
                onVerified = { navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.VERIFY_EMAIL) { inclusive = true } } },
                onLogIn = { navController.navigate(Routes.LOG_IN) { popUpTo(Routes.WELCOME) } },
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
                onOpenTalk = { navController.navigate(Routes.TALK) },
                onOpenRadio = { navController.navigate(Routes.RADIO) },
                onOpenAcademy = { navController.navigate(Routes.ACADEMY) },
                onOpenMarketplace = { navController.navigate(Routes.MARKETPLACE) },
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
                onOpenProfileEdit = { navController.navigate(Routes.PROFILE_EDIT) },
                onOpenFloorTab = { navController.navigate(Routes.FLOOR) },
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
                        com.thefloor.app.domain.DeepLinkParser.Target.ProfileEdit ->
                            navController.navigate(Routes.PROFILE_EDIT)
                        is com.thefloor.app.domain.DeepLinkParser.Target.Profile ->
                            navController.navigate(Routes.publicProfile(target.userId))
                        is com.thefloor.app.domain.DeepLinkParser.Target.Job ->
                            navController.navigate(Routes.JOBS)
                        is com.thefloor.app.domain.DeepLinkParser.Target.Course ->
                            navController.navigate(Routes.ACADEMY)
                        is com.thefloor.app.domain.DeepLinkParser.Target.Deal ->
                            navController.navigate(Routes.MARKETPLACE)
                        com.thefloor.app.domain.DeepLinkParser.Target.Home ->
                            navController.navigate(Routes.HOME)
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
        composable(Routes.PULSE) {
            PulseScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.JOBS) { com.thefloor.app.feature.pages.JobsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ACADEMY) { com.thefloor.app.feature.pages.AcademyScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.MARKETPLACE) { com.thefloor.app.feature.pages.MarketplaceScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.INSIGHTS) { com.thefloor.app.feature.pages.InsightsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ABOUT) { com.thefloor.app.feature.pages.AboutScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.EVENTS) { com.thefloor.app.feature.pages.EventsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.RESOURCES) { com.thefloor.app.feature.pages.ResourcesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.WELLBEING) { com.thefloor.app.feature.pages.WellbeingScreen(onBack = { navController.popBackStack() }) }
    }
}
