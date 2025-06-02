package io.github.luissimas.core.shorturl.usecases

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.driven.ShortCodeGenerator
import io.github.luissimas.core.shorturl.ports.driven.ShortUrlRepository
import io.github.luissimas.core.shorturl.ports.driver.CreateShortUrlService
import io.github.oshai.kotlinlogging.KotlinLogging

val logger = KotlinLogging.logger { }

class CreateShortUrlUseCase(
    private val repository: ShortUrlRepository,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val maxAttempts: Int = 5,
) : CreateShortUrlService {
    override fun invoke(longUrl: String): ShortUrl {
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
}
