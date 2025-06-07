package io.github.luissimas.core.shorturl.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import io.github.luissimas.core.shorturl.domain.ApplicationError
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
    fun createShortUrl(longUrl: Url): Either<ApplicationError, ShortUrl> =
        either {
            repeat(maxAttempts) {
                val shortCode = shortCodeGenerator.generate().bind()
                val shortUrl = ShortUrl(shortCode = shortCode, longUrl = longUrl)
                val saveResult = repository.save(shortUrl)

                when (saveResult) {
                    is Either.Right -> return shortUrl.right()
                    is Either.Left -> {
                        if (saveResult.value != ApplicationError.ShortCodeAlreadyExists) return saveResult
                        logger.atWarn {
                            message = "Generated short code already exists"
                            payload = mapOf("attempt" to it, "maxAttempts" to maxAttempts, "shortCode" to shortCode)
                        }
                    }
                }
            }

            raise(ApplicationError.MaxAttemptsReached)
        }

    fun getShortUrl(shortCode: ShortCode): Either<ApplicationError, ShortUrl> = repository.getByShortCode(shortCode)
}
