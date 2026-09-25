package com.thefloor.app.core.demo

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** Answers every API call locally when [DemoMode] is on, so the app is fully explorable with no backend. */
@Singleton
class DemoInterceptor @Inject constructor(
    private val backend: DemoBackend,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!DemoMode.enabled) return chain.proceed(request)

        val segments = request.url.encodedPath.trim('/').split('/').filter { it.isNotBlank() }
            .let { if (it.firstOrNull() == "v1") it.drop(1) else it }
        val requestBody = request.body?.let { body ->
            okio.Buffer().also { runCatching { body.writeTo(it) } }.readUtf8()
        }.orEmpty()

        val json = backend.handle(request.method, segments, request.url::queryParameter, requestBody)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (demo)")
            .body(json.toResponseBody(JSON_MEDIA))
            .build()
    }

    private companion object {
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

/** The demo's mutable world. */
