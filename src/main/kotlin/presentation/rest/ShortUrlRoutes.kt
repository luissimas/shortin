package io.github.luissimas.presentation.rest

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.onFailure
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.services.ShortUrlService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class CreateShortUrlRequest(
    val longUrl: String?,
)

@Serializable
data class CreateShortUrlResponse(
    val shortCode: String,
    val longUrl: String,
) {
    companion object {
        fun fromShortUrl(shortUrl: ShortUrl) = CreateShortUrlResponse(shortUrl.shortCode.code, shortUrl.longUrl.url)
    }
}

suspend fun handleError(
    call: RoutingCall,
    error: Failure<Any>,
) {
    val status: HttpStatusCode =
        when (error.reason) {
            is DomainError -> HttpStatusCode.BadRequest
            is ApplicationError.EntityNotFound -> HttpStatusCode.NotFound
            is ApplicationError.ShortCodeAlreadyExists -> HttpStatusCode.Conflict
            else -> HttpStatusCode.InternalServerError
        }
    call.respond(status)
}

fun Route.shortUrl(shortUrlService: ShortUrlService) =
    route("/") {
        post("/urls") {
            val request = call.receive<CreateShortUrlRequest>()
            val longUrl = Url.create(request.longUrl).onFailure { return@post handleError(call, it) }
            val shortUrl = shortUrlService.createShortUrl(longUrl).onFailure { return@post handleError(call, it) }

            call.respond(HttpStatusCode.Created, CreateShortUrlResponse.fromShortUrl(shortUrl))
        }

        get("/r/{shortCode}") {
            val shortCodeParam = call.pathParameters["shortCode"]
            val shortCode = ShortCode.create(shortCodeParam).onFailure { return@get handleError(call, it) }
            val shortUrl = shortUrlService.getShortUrl(shortCode).onFailure { return@get handleError(call, it) }

            call.respondRedirect(shortUrl.longUrl.url, permanent = false)
        }
    }
