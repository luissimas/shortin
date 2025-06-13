package io.github.luissimas.presentation.rest

import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ApplicationError.CouldNotAllocateShortCode
import io.github.luissimas.core.shorturl.domain.ApplicationError.EntityNotFound
import io.github.luissimas.core.shorturl.domain.ApplicationError.ShortCodeAlreadyExists
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.DomainError.InvalidShortCode
import io.github.luissimas.core.shorturl.domain.DomainError.InvalidUrl
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

class HttpException(
    val status: HttpStatusCode,
    val error: HttpError,
) : RuntimeException()

@Serializable
data class HttpError(
    val error: String,
)

fun DomainError.asHttpException(): HttpException =
    when (this) {
        is InvalidUrl -> HttpException(HttpStatusCode.BadRequest, HttpError("Invalid URL"))
        is InvalidShortCode -> HttpException(HttpStatusCode.BadRequest, HttpError("Invalid short code"))
    }

fun ApplicationError.asHttpException(): HttpException =
    when (this) {
        is CouldNotAllocateShortCode ->
            HttpException(
                HttpStatusCode.InternalServerError,
                HttpError("Could not allocate short code"),
            )
        is ShortCodeAlreadyExists -> HttpException(HttpStatusCode.Conflict, HttpError("Short code already exists"))
        is EntityNotFound -> HttpException(HttpStatusCode.NotFound, HttpError("Not found"))
    }
