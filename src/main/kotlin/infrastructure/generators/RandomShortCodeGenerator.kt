package io.github.luissimas.infrastructure.generators

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.ports.ShortCodeGenerator
import java.security.SecureRandom

/**
 * Generator for short codes for URLs.
 *
 * Short codes are randomly generated and Base62 encoded to ensure
 * they are safe to use in URLs.
 */
class RandomShortCodeGenerator(
    private val codeLength: Int = 8,
) : ShortCodeGenerator {
    private val random = SecureRandom()
    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    /**
     * Generate a new short code with the given `length`.
     */
    override fun generate(): Either<ApplicationError, ShortCode> {
        val code =
            buildString(codeLength) {
                repeat(codeLength) {
                    val index = random.nextInt(alphabet.length)
                    append(alphabet[index])
                }
            }
        return ShortCode
            .create(code)
            .getOrElse {
                throw IllegalStateException("Generated invalid short code")
            }.right()
    }
}
