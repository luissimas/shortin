package io.github.luissimas.domain.shorturl

import io.github.oshai.kotlinlogging.KotlinLogging

val logger = KotlinLogging.logger { }

class CreateShortUrlService(
    private val repository: ShortUrlRepository,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val maxAttempts: Int = 5,
) : CreateShortUrlUsecase {
    override fun createShortUrl(longUrl: String): ShortUrl {
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

        throw IllegalStateException("Unable to generate unique short code after $maxAttempts attempts")
    }
}