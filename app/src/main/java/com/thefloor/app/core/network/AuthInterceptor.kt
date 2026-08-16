package com.thefloor.app.core.network

import com.thefloor.app.core.datastore.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Attaches the bearer token to every request except public auth/config calls. */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isPublic = (path.startsWith("/v1/auth/") && path != "/v1/auth/verify/resend") ||
            path == "/v1/config" ||
            path.startsWith("/v1/referrals/resolve/")
        if (isPublic) return chain.proceed(request)

        val session = runBlocking { sessionStore.current() } ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
        )
    }
}
