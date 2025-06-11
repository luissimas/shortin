package io.github.luissimas.core.shorturl.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.peek
import io.github.luissimas.core.shorturl.domain.ApplicationError.CouldNotAllocateShortCode
import io.github.luissimas.core.shorturl.domain.ApplicationError.EntityNotFound
import io.github.luissimas.core.shorturl.domain.Event
import io.github.luissimas.core.shorturl.domain.EventPublisher
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.ports.ShortCodeGenerator
import io.github.luissimas.core.shorturl.ports.ShortUrlRepository
import io.github.oshai.kotlinlogging.KotlinLogging

val logger = KotlinLogging.logger { }

class ShortUrlService(
    private val repository: ShortUrlRepository,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val eventPublisher: EventPublisher,
    private val maxAttempts: Int = 5,
) {
    suspend fun createShortUrl(longUrl: Url): Result<ShortUrl, CouldNotAllocateShortCode> {
        repeat(maxAttempts) {
            val shortCode = shortCodeGenerator.generate()
            val shortUrl = ShortUrl(shortCode = shortCode, longUrl = longUrl)

            when (val saveResult = repository.save(shortUrl)) {
                is Success -> return saveResult
                is Failure -> {
                    logger.atWarn {
                        message = "Generated short code already exists"
                        payload = mapOf("attempt" to it, "maxAttempts" to maxAttempts, "shortCode" to shortCode)
                    }
                }
            }
        }

        return Failure(CouldNotAllocateShortCode)
    }

    suspend fun getShortUrl(shortCode: ShortCode): Result<ShortUrl, EntityNotFound> =
        repository.getByShortCode(shortCode).peek {
            eventPublisher.publish(Event.ShortUrlAccessed(it))
        }
}
