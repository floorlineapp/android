package com.thefloor.app.referral

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.thefloor.app.core.datastore.ReferralStore
import com.thefloor.app.domain.Validators
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-launch referral attribution via the Play Install Referrer API.
 * The Play redirect carries `referrer=utm_source%3Dfloor%26floor_ref%3D{CODE}`;
 * we extract floor_ref and stash it for signup. The server independently
 * validates against click records — this value is transport, not truth.
 */
@Singleton
class InstallReferrerHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val referralStore: ReferralStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Idempotent; call once from Application.onCreate. Never blocks or crashes launch. */
    fun captureOnce() {
        val prefs = context.getSharedPreferences("floor_install_referrer", Context.MODE_PRIVATE)
        if (prefs.getBoolean("captured", false)) return

        val client = InstallReferrerClient.newBuilder(context).build()
        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val referrer = client.installReferrer.installReferrer // e.g. utm_source=floor&floor_ref=ABC123
                        val code = referrer
                            .split('&')
                            .map { it.split('=', limit = 2) }
                            .firstOrNull { it.size == 2 && it[0] == "floor_ref" }
                            ?.get(1)
                        if (code != null && Validators.referralCodeFormat(code)) {
                            scope.launch { referralStore.save(code, "INSTALL_REFERRER") }
                            Timber.i("Install referrer captured")
                        }
                    }
                    prefs.edit().putBoolean("captured", true).apply()
                } catch (e: Exception) {
                    Timber.w(e, "Install referrer read failed")
                } finally {
                    runCatching { client.endConnection() }
                }
            }

            override fun onInstallReferrerServiceDisconnected() = Unit
        })
    }
}
