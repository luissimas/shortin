package io.github.luissimas.presentation.rest

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.driver.CreateShortUrlService
import io.github.luissimas.core.shorturl.ports.driver.GetShortUrlService
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
    val longUrl: String,
)

@Serializable
data class CreateShortUrlResponse(
    val shortCode: String,
    val longUrl: String,
) {
    companion object {
        fun fromShortUrl(shortUrl: ShortUrl) = CreateShortUrlResponse(shortUrl.shortCode, shortUrl.longUrl)
    }
}

fun Route.shortUrl(
    createShortUrlService: CreateShortUrlService,
    getShortUrlService: GetShortUrlService,
) = route("/") {
    post("/urls") {
        val request = call.receive<CreateShortUrlRequest>()
        val shortUrl = createShortUrlService(request.longUrl)
        val response = CreateShortUrlResponse.fromShortUrl(shortUrl)
        call.respond(HttpStatusCode.Created, response)
    }

    get("/r/{shortCode}") {
        val shortCode = call.pathParameters["shortCode"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val shortUrl = getShortUrlService(shortCode) ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respondRedirect(shortUrl.longUrl, permanent = false)
    }
}
