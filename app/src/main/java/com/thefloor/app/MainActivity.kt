package com.thefloor.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.datastore.ReferralStore
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.domain.DeepLinkParser
import com.thefloor.app.navigation.FloorApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var referralStore: ReferralStore
    @Inject lateinit var analytics: AnalyticsTracker

    /** Latest deep-link target; the nav host consumes and clears it. */
    private val pendingDeepLink = MutableStateFlow<DeepLinkParser.Target?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Guard against configuration changes / process restore: the launch
        // intent must only be interpreted once, or rotation re-fires deep links
        // and inflates app_open counts.
        if (savedInstanceState == null) {
            analytics.track(Events.APP_OPEN)
            handleIntent(intent)
        }

        setContent {
            FloorTheme {
                FloorApp(
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.dataString ?: return
        val target = DeepLinkParser.parse(uri)
        // Referral links must survive the signup flow even if the user wanders:
        // persist the code the moment the link opens the app.
        if (target is DeepLinkParser.Target.Invite) {
            lifecycleScope.launch { referralStore.save(target.referralCode, "DEEP_LINK") }
            analytics.track(Events.REFERRAL_CODE_CAPTURED, mapOf("source" to "deep_link"))
        }
        pendingDeepLink.value = target
    }
}
