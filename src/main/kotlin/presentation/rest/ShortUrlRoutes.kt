package io.github.luissimas.presentation.rest

import dev.forkhandles.result4k.onFailure
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.services.ShortUrlService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
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

fun Route.shortUrl(shortUrlService: ShortUrlService) =
    route("/") {
        post("/urls") {
            val request = call.receive<CreateShortUrlRequest>()
            val longUrl = Url.create(request.longUrl).onFailure { throw it.reason.asHttpException() }
            val shortUrl = shortUrlService.createShortUrl(longUrl).onFailure { throw it.reason.asHttpException() }

            call.respond(HttpStatusCode.Created, CreateShortUrlResponse.fromShortUrl(shortUrl))
        }

        get("/r/{shortCode}") {
            val shortCodeParam = call.pathParameters["shortCode"]
            val shortCode = ShortCode.create(shortCodeParam).onFailure { throw it.reason.asHttpException() }
            val shortUrl = shortUrlService.getShortUrl(shortCode).onFailure { throw it.reason.asHttpException() }

            call.respondRedirect(shortUrl.longUrl.url, permanent = false)
        }
    }
