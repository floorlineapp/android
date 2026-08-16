package com.thefloor.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.data.ConfigRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.domain.DeepLinkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Session gate: which world the user is in. */
enum class SessionState { LOADING, SIGNED_OUT, SIGNED_IN, SIGNED_IN_UNVERIFIED }

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.session
        .map { session ->
            when {
                session == null -> SessionState.SIGNED_OUT
                !session.emailVerified -> SessionState.SIGNED_IN_UNVERIFIED
                else -> SessionState.SIGNED_IN
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.LOADING)

    val radioEnabled = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            radioEnabled.value = configRepository.flags()["radio"] == true
        }
    }
}

private val authRoutesSet = setOf(
    Routes.WELCOME, Routes.SIGN_UP, Routes.LOG_IN, Routes.VERIFY_EMAIL,
    Routes.FORGOT_PASSWORD, Routes.RESET_PASSWORD, Routes.ONBOARDING,
)

private data class TabSpec(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabSpec(Routes.HOME, "Home", Icons.Filled.Home),
    TabSpec(Routes.FLOOR, "Floor", Icons.Filled.Layers),
    TabSpec(Routes.TALK, "Talk", Icons.Filled.Forum),
    TabSpec(Routes.RADIO, "Radio", Icons.Filled.Radio),
    TabSpec(Routes.MORE, "More", Icons.Filled.GridView),
)

@Composable
fun FloorApp(
    pendingDeepLink: MutableStateFlow<DeepLinkParser.Target?>,
    onDeepLinkConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val rootViewModel: RootViewModel = hiltViewModel()
    val sessionState by rootViewModel.sessionState.collectAsStateWithLifecycle()
    val radioEnabled by rootViewModel.radioEnabled.collectAsStateWithLifecycle()
    val deepLink by pendingDeepLink.collectAsState()

    // Deep links: park until session state is known, then route.
    LaunchedEffect(deepLink, sessionState) {
        val target = deepLink ?: return@LaunchedEffect
        if (sessionState == SessionState.LOADING) return@LaunchedEffect
        navigateToTarget(navController, target, sessionState)
        onDeepLinkConsumed()
    }

    // Session transitions: restored session skips Welcome; logout clears the stack.
    LaunchedEffect(sessionState) {
        val route = navController.currentBackStackEntry?.destination?.route
        when {
            sessionState == SessionState.SIGNED_IN && route == Routes.WELCOME ->
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            // Restored-but-unverified sessions must pass the verify gate,
            // never land on Home.
            sessionState == SessionState.SIGNED_IN_UNVERIFIED && route == Routes.WELCOME ->
                navController.navigate(Routes.verifyEmail()) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            sessionState == SessionState.SIGNED_OUT &&
                route != null && !authRoutesSet.contains(route) ->
                navController.navigate(Routes.WELCOME) {
                    popUpTo(0) { inclusive = true }
                }
            else -> Unit
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val visibleTabs = if (radioEnabled) tabs else tabs.filterNot { it.route == Routes.RADIO }
    val showBottomBar = currentRoute != null && visibleTabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = FloorTheme.colors.surface) {
                    visibleTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    // WELCOME (the graph start) is popped inclusively
                                    // once signed in — anchor tab stacks on HOME.
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = FloorTheme.typography.caption) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FloorTheme.colors.amber,
                                selectedTextColor = FloorTheme.colors.amber,
                                unselectedIconColor = FloorTheme.colors.textMuted,
                                unselectedTextColor = FloorTheme.colors.textMuted,
                                indicatorColor = FloorTheme.colors.amberSoft,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        FloorNavHost(
            navController = navController,
            sessionState = sessionState,
            modifier = Modifier.padding(padding),
        )
    }
}

private fun navigateToTarget(
    navController: NavHostController,
    target: DeepLinkParser.Target,
    sessionState: SessionState,
) {
    if (sessionState == SessionState.SIGNED_OUT) {
        when (target) {
            is DeepLinkParser.Target.Invite ->
                navController.navigate(Routes.signUp(target.referralCode)) { launchSingleTop = true }
            is DeepLinkParser.Target.ResetPassword ->
                navController.navigate(Routes.resetPassword(target.token)) { launchSingleTop = true }
            is DeepLinkParser.Target.VerifyEmail ->
                // Confirm endpoint is public — the token can be consumed pre-login.
                // No launchSingleTop: a fresh entry carries the fresh token.
                navController.navigate(Routes.verifyEmail(target.token))
            else -> Unit // Auth flow first; other targets are dropped by design.
        }
        return
    }
    when (target) {
        is DeepLinkParser.Target.Invite, DeepLinkParser.Target.InviteEarn ->
            navController.navigate(Routes.INVITE_EARN) { launchSingleTop = true }
        is DeepLinkParser.Target.Floor ->
            navController.navigate(Routes.communityDetail(target.communityId)) { launchSingleTop = true }
        DeepLinkParser.Target.FloorTab ->
            navController.navigate(Routes.FLOOR) { launchSingleTop = true }
        is DeepLinkParser.Target.TalkPost ->
            navController.navigate(Routes.postDetail(target.postId)) { launchSingleTop = true }
        is DeepLinkParser.Target.Job -> navController.navigate(Routes.JOBS) { launchSingleTop = true }
        is DeepLinkParser.Target.Course -> navController.navigate(Routes.ACADEMY) { launchSingleTop = true }
        is DeepLinkParser.Target.Deal -> navController.navigate(Routes.MARKETPLACE) { launchSingleTop = true }
        DeepLinkParser.Target.ProfileEdit ->
            navController.navigate(Routes.PROFILE_EDIT) { launchSingleTop = true }
        is DeepLinkParser.Target.Profile ->
            navController.navigate(Routes.publicProfile(target.userId)) { launchSingleTop = true }
        DeepLinkParser.Target.Rewards -> navController.navigate(Routes.REWARDS) { launchSingleTop = true }
        // Verify links carry a token that must be consumed server-side —
        // route to the Verify screen in every session state.
        is DeepLinkParser.Target.VerifyEmail ->
            navController.navigate(Routes.verifyEmail(target.token)) // fresh entry, fresh token
        is DeepLinkParser.Target.ResetPassword,
        DeepLinkParser.Target.Home -> navController.navigate(Routes.HOME) { launchSingleTop = true }
    }
}
