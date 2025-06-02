package io.github.luissimas

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.usecases.CreateShortUrlUseCase
import io.github.luissimas.infrastructure.generators.RandomShortCodeGenerator
import io.github.luissimas.infrastructure.persistence.InMemoryShortUrlRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class CreateShortUrlRequest(
    val longUrl: String,
)

@Serializable
data class CreateShortUrlResponse(
    val shortCode: String,
    val longUrl: String,
)

fun ShortUrl.toResponse(): CreateShortUrlResponse = CreateShortUrlResponse(this.shortCode, this.longUrl)

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureRouting() {
    val createShortUrlService = CreateShortUrlUseCase(InMemoryShortUrlRepository(), RandomShortCodeGenerator())

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello")) {
                ValidationResult.Invalid("Body text should start with 'Hello'")
            } else {
                ValidationResult.Valid
            }
        }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/url") {
            val request = call.receive<CreateShortUrlRequest>()
            val shortUrl = createShortUrlService.createShortUrl(request.longUrl)

            call.respond(shortUrl.toResponse())
        }
    }
}
