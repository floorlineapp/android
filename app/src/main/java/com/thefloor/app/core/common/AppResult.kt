package com.thefloor.app.core.common

/** Result of any repository operation. Errors carry user-presentable messages. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    val userMessage: String

    /** No connectivity / timeouts — cached data may still be shown. */
    data class Network(override val userMessage: String = "Can't reach The Floor. Check your connection.") : AppError

    /** Session is gone; the UI should route to login. */
    data class Unauthorized(override val userMessage: String = "Please log in again.") : AppError

    /** Server rejected the input; code is the stable API error code. */
    data class Api(val code: String, override val userMessage: String) : AppError

    data class Unknown(override val userMessage: String = "Something went wrong.") : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onError(block: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) block(error)
    return this
}
