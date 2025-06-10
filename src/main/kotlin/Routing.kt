package io.github.luissimas

import io.github.luissimas.core.shorturl.services.ShortUrlService
import io.github.luissimas.infrastructure.generators.RandomShortCodeGenerator
import io.github.luissimas.infrastructure.persistence.SqlDelightDatabase
import io.github.luissimas.infrastructure.persistence.SqlDelightShortUrlRepository
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

val logger = KotlinLogging.logger { }

fun Application.configureRouting(database: SqlDelightDatabase) {
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

    val shortUrlRepository = SqlDelightShortUrlRepository(database)
    val shortUrlService = ShortUrlService(shortUrlRepository, RandomShortCodeGenerator())

    routing {
        shortUrl(shortUrlService)
    }
}
