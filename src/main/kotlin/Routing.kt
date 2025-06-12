package io.github.luissimas

import io.github.luissimas.core.shorturl.services.ShortUrlService
import io.github.luissimas.infrastructure.generators.RandomShortCodeGenerator
import io.github.luissimas.infrastructure.messaging.KafkaEventPublisher
import io.github.luissimas.infrastructure.messaging.KafkaTopic
import io.github.luissimas.infrastructure.persistence.SqlDelightDatabase
import io.github.luissimas.infrastructure.persistence.SqlDelightShortUrlRepository
import io.github.luissimas.presentation.rest.HttpError
import io.github.luissimas.presentation.rest.HttpException
import io.github.luissimas.presentation.rest.shortUrl
import io.github.nomisRev.kafka.publisher.PublisherSettings
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing

val logger = KotlinLogging.logger { }

fun Application.configureRouting(
    database: SqlDelightDatabase,
    kafkaTopic: KafkaTopic,
    kafkaPublisherSettings: PublisherSettings<String, String>,
) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.atError {
                message = "Unhandled exception"
                payload = mapOf("exception" to cause.message)
            }

            call.respond(HttpStatusCode.InternalServerError, HttpError(cause.message ?: "Unknown error"))
        }

        exception<HttpException> { call, cause ->
            call.respond(cause.status, cause.error)
        }
    }

    val eventPublisher = KafkaEventPublisher(kafkaTopic, kafkaPublisherSettings)
    val shortUrlRepository = SqlDelightShortUrlRepository(database)
    val shortUrlService = ShortUrlService(shortUrlRepository, RandomShortCodeGenerator(), eventPublisher)

    routing {
        shortUrl(shortUrlService)
    }
}
