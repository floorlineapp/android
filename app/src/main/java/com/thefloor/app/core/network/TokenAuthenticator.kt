package com.thefloor.app.core.network

import com.thefloor.app.core.datastore.SessionStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Transparent refresh-token rotation on 401. Refresh runs on a bare client
 * (no interceptor loop); rotation failure clears the session, which the UI
 * observes and routes to login.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionStore: SessionStore,
    @Named("baseUrl") private val baseUrl: String,
) : Authenticator {

    private val bareClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Never try to refresh the refresh call itself, and give up after one
        // retry. Other /v1/auth/ paths (e.g. verify/resend) ARE auth-protected
        // and must be refreshable like any endpoint.
        if (response.request.url.encodedPath == "/v1/auth/refresh") return null
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val session = runBlocking { sessionStore.current() } ?: return null

            // Another thread may have refreshed while we waited on the lock.
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (sentToken != null && sentToken != session.accessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .build()
            }

            val refreshed = runBlocking { refresh(session.refreshToken) }
            return if (refreshed != null) {
                runBlocking { sessionStore.updateTokens(refreshed.accessToken, refreshed.refreshToken) }
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshed.accessToken}")
                    .build()
            } else {
                Timber.i("Refresh rotation failed — clearing session")
                runBlocking { sessionStore.clear() }
                null
            }
        }
    }

    private fun refresh(refreshToken: String): TokenPairDto? = try {
        val body = json.encodeToString(RefreshRequestDto.serializer(), RefreshRequestDto(refreshToken))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${baseUrl}v1/auth/refresh")
            .post(body)
            .build()
        bareClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) null
            else resp.body?.string()?.let { json.decodeFromString(TokenPairDto.serializer(), it) }
        }
    } catch (e: Exception) {
        Timber.d(e, "Refresh call failed")
        null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
