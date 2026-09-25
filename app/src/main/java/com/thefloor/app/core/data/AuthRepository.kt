package com.thefloor.app.core.data

import com.thefloor.app.core.common.AppResult
import com.thefloor.app.core.common.map
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.datastore.ReferralStore
import com.thefloor.app.core.datastore.Session
import com.thefloor.app.core.datastore.SessionStore
import com.thefloor.app.core.demo.DemoStore
import com.thefloor.app.core.network.EmailBodyDto
import com.thefloor.app.core.network.FloorApi
import com.thefloor.app.core.network.LoginRequestDto
import com.thefloor.app.core.network.RefreshRequestDto
import com.thefloor.app.core.network.ResetRequestDto
import com.thefloor.app.core.network.SignupRequestDto
import com.thefloor.app.core.network.TokenBodyDto
import com.thefloor.app.core.network.safeCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuthRepository @Inject constructor(
    private val api: FloorApi,
    private val sessionStore: SessionStore,
    private val referralStore: ReferralStore,
    private val demoStore: DemoStore,
) {
    /** Null = signed out. */
    val session: Flow<Session?> = sessionStore.session

    /** Signs up, transporting any pending referral code. */
    suspend fun signUp(email: String, password: String, displayName: String): AppResult<Unit> {
        val pending = referralStore.pending()
        return safeCall {
            api.signup(
                SignupRequestDto(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim(),
                    referralCode = pending?.first,
                    referralSource = pending?.second,
                )
            )
        }.onSuccess { response ->
            sessionStore.save(
                userId = response.userId,
                accessToken = response.tokens.accessToken,
                refreshToken = response.tokens.refreshToken,
                emailVerified = response.emailVerified,
            )
            referralStore.clear()
        }.map { }
    }

    suspend fun logIn(email: String, password: String): AppResult<Unit> =
        safeCall { api.login(LoginRequestDto(email.trim(), password)) }
            .onSuccess { response ->
                sessionStore.save(
                    userId = response.userId,
                    accessToken = response.tokens.accessToken,
                    refreshToken = response.tokens.refreshToken,
                    emailVerified = response.emailVerified,
                )
            }.map { }

    suspend fun logOut() {
        val session = sessionStore.current()
        if (session != null) {
            safeCall { api.logout(RefreshRequestDto(session.refreshToken)) }
        }
        sessionStore.clear()
        demoStore.set(false)
    }

    suspend fun resendVerification(): AppResult<Unit> =
        safeCall { api.resendVerification() }.map { }

    suspend fun confirmVerification(token: String): AppResult<Unit> =
        safeCall { api.confirmVerification(TokenBodyDto(token)) }
            .onSuccess { sessionStore.markVerified() }
            .map { }

    suspend fun refreshVerificationStatus(): AppResult<Boolean> =
        safeCall { api.me() }.onSuccess { profile ->
            if (profile.emailVerified) sessionStore.markVerified()
        }.map { it.emailVerified }

    suspend fun forgotPassword(email: String): AppResult<Unit> =
        safeCall { api.forgotPassword(EmailBodyDto(email.trim())) }.map { }

    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> =
        safeCall { api.resetPassword(ResetRequestDto(token, newPassword)) }.map { }
}
