package io.github.luissimas.core.shorturl.services

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
    private val maxAttempts: Int = 5,
) {
    fun createShortUrl(longUrl: Url): ShortUrl {
        logger.atDebug {
            message = "Creating short URL"
            payload = mapOf("longUrl" to longUrl)
        }

        repeat(maxAttempts) {
            val shortCode = shortCodeGenerator.generate()
            val existingShortUrl = repository.getByShortCode(shortCode)

            if (existingShortUrl == null) {
                val shortUrl = ShortUrl(shortCode = shortCode, longUrl = longUrl)
                repository.save(shortUrl)

                logger.atDebug {
                    message = "Created short URL"
                    payload = mapOf("shortUrl" to shortUrl)
                }

                return shortUrl
            }

            logger.atWarn {
                message = "Generated short code already exists"
                payload = mapOf("attempt" to it, "maxAttempts" to maxAttempts, "shortCode" to shortCode)
            }
        }

        error("Unable to generate unique short code after $maxAttempts attempts")
    }

    fun getShortUrl(shortCode: ShortCode): ShortUrl? = repository.getByShortCode(shortCode)
}
