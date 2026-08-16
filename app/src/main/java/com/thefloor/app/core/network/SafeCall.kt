package com.thefloor.app.core.network

import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.AppResult
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

private val errorJson = Json { ignoreUnknownKeys = true }

/**
 * Single translation point from transport failures to domain errors.
 * Repositories wrap every API call in this.
 */
suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: HttpException) {
    val parsed = try {
        e.response()?.errorBody()?.string()?.let { errorJson.decodeFromString<ApiErrorDto>(it) }
    } catch (_: Exception) {
        null
    }
    when {
        e.code() == 401 -> AppResult.Error(AppError.Unauthorized())
        parsed != null -> AppResult.Error(AppError.Api(parsed.code, parsed.message))
        else -> AppResult.Error(AppError.Unknown())
    }
} catch (e: IOException) {
    Timber.d(e, "Network failure")
    AppResult.Error(AppError.Network())
} catch (e: Exception) {
    // Never log request/response bodies here — they can contain credentials.
    Timber.w(e, "Unexpected API failure: %s", e.javaClass.simpleName)
    AppResult.Error(AppError.Unknown())
}
