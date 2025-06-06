package io.github.luissimas

import io.github.luissimas.core.shorturl.usecases.CreateShortUrlUseCase
import io.github.luissimas.core.shorturl.usecases.GetShortUrlUseCase
import io.github.luissimas.infrastructure.adapters.driven.generators.RandomShortCodeGenerator
import io.github.luissimas.infrastructure.adapters.driven.persistence.InMemoryShortUrlRepository
import io.github.luissimas.presentation.rest.shortUrl
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi

val logger = KotlinLogging.logger { }

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.atError {
                message = "Unhandled exception"
                payload = mapOf("exception" to cause.message)
            }

            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

        exception<ContentTransformationException> { call, cause ->
            call.respondText(text = "Bad request", status = HttpStatusCode.BadRequest)
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

    val shortUrlRepository = InMemoryShortUrlRepository()
    val createShortUrlService = CreateShortUrlUseCase(shortUrlRepository, RandomShortCodeGenerator())
    val getShortUrlService = GetShortUrlUseCase(shortUrlRepository)

    routing {
        shortUrl(createShortUrlService, getShortUrlService)
    }
}
