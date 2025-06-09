package io.github.luissimas.presentation.rest

import arrow.core.raise.either
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.domain.ValidationError
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
    error: DomainError,
) {
    val status: HttpStatusCode =
        when (error) {
            is ValidationError -> HttpStatusCode.BadRequest
            is ApplicationError.EntityNotFound -> HttpStatusCode.NotFound
            is ApplicationError.ShortCodeAlreadyExists -> HttpStatusCode.Conflict
            else -> HttpStatusCode.InternalServerError
        }
    call.respond(status)
}

fun Route.shortUrl(shortUrlService: ShortUrlService) =
    route("/") {
        post("/urls") {
            either {
                val request = call.receive<CreateShortUrlRequest>()
                val longUrl = Url.create(request.longUrl).bind()
                val shortUrl = shortUrlService.createShortUrl(longUrl).bind()
                CreateShortUrlResponse.fromShortUrl(shortUrl)
            }.fold(
                ifLeft = { handleError(call, it) },
                ifRight = { call.respond(HttpStatusCode.Created, it) },
            )
        }

        get("/r/{shortCode}") {
            either {
                val shortCode = ShortCode.create(call.pathParameters["shortCode"]).bind()
                shortUrlService.getShortUrl(shortCode).bind()
            }.fold(
                ifLeft = { handleError(call, it) },
                ifRight = { call.respondRedirect(it.longUrl.url, permanent = false) },
            )
        }
    }
