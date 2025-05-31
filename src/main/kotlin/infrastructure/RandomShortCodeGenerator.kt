package io.github.luissimas.infrastructure

import io.github.luissimas.domain.shorturl.ShortCodeGenerator
import java.security.SecureRandom

/**
 * Generator for short codes for URLs.
 *
 * Short codes are randomly generated and Base62 encoded to ensure
 * they are safe to use in URLs.
 */
class RandomShortCodeGenerator(private val codeLength: Int = 8) : ShortCodeGenerator {
    private val random = SecureRandom()
    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    /**
     * Generate a new short code with the given `length`.
     */
    override fun generate(): String =
        buildString(codeLength) {
            repeat(codeLength) {
                val index = random.nextInt(alphabet.length)
                append(alphabet[index])
            }
        }
}