package io.github.luissimas.domain.shorturl

import io.github.luissimas.infrastructure.RandomShortCodeGenerator
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class RandomShortCodeGeneratorTest {
    private val generator = RandomShortCodeGenerator()

    @Test
    fun `Created short code is URL safe`() {
        // TODO: use a property-based testing framework
        repeat(10_000) {
            val shortCode = generator.generate()
            val encoded = URLEncoder.encode(shortCode, Charset.defaultCharset())

            assertEquals(shortCode, encoded)
        }
    }

    @Test
    fun `Created short code has the specified length`() {
        val random = SecureRandom()

        // TODO: use a property-based testing framework
        repeat(10_000) {
            val codeLength = random.nextInt(1000)
            val generator = RandomShortCodeGenerator(codeLength)
            val shortCode = generator.generate()

            assertEquals(shortCode.length, codeLength)
        }
    }
}