package com.thefloor.app.core.network

import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.AppResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeCallTest {

    @Test
    fun `success passes data through`() = runTest {
        val result = safeCall { "ok" }
        assertEquals(AppResult.Success("ok"), result)
    }

    @Test
    fun `io exception becomes network error`() = runTest {
        val result = safeCall<String> { throw IOException("boom") }
        assertTrue((result as AppResult.Error).error is AppError.Network)
    }

    @Test
    fun `401 becomes unauthorized`() = runTest {
        val response = Response.error<String>(401, "{}".toResponseBody("application/json".toMediaType()))
        val result = safeCall<String> { throw HttpException(response) }
        assertTrue((result as AppResult.Error).error is AppError.Unauthorized)
    }

    @Test
    fun `structured api error carries code and message`() = runTest {
        val body = """{"code":"EMAIL_IN_USE","message":"An account with this email already exists"}"""
            .toResponseBody("application/json".toMediaType())
        val response = Response.error<String>(409, body)
        val result = safeCall<String> { throw HttpException(response) }
        val error = (result as AppResult.Error).error as AppError.Api
        assertEquals("EMAIL_IN_USE", error.code)
        assertEquals("An account with this email already exists", error.userMessage)
    }

    @Test
    fun `unparseable error body degrades to unknown`() = runTest {
        val response = Response.error<String>(500, "<html>oops</html>".toResponseBody("text/html".toMediaType()))
        val result = safeCall<String> { throw HttpException(response) }
        assertTrue((result as AppResult.Error).error is AppError.Unknown)
    }
}
