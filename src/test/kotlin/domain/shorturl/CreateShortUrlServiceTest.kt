package io.github.luissimas.domain.shorturl

import io.github.luissimas.infrastructure.InMemoryShortUrlRepository
import io.github.luissimas.infrastructure.SequenceShortCodeGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CreateShortUrlServiceTest {
    @Test
    fun `Should create a short URL and save it on the repository`() {
        val repository = InMemoryShortUrlRepository()
        val shortCodeGenerator = SequenceShortCodeGenerator(listOf("any-code").iterator())
        val service = CreateShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

        val longUrl = "any-long-url"
        val shortUrl = service.createShortUrl(longUrl)
        val storedShortUrl = repository.getByShortCode(shortUrl.shortCode)

        assertEquals(ShortUrl(shortCode = "any-code", longUrl = longUrl), shortUrl)
        assertNotNull(storedShortUrl)
        assertEquals(shortUrl, storedShortUrl)
    }

    @Test
    fun `Should retry short code creation on short code conflicts`() {
        val repository = InMemoryShortUrlRepository()
        val shortCodeGenerator = SequenceShortCodeGenerator(listOf("any-code", "another-code").iterator())
        val service = CreateShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

        repository.save(ShortUrl(shortCode = "any-code", longUrl = "any-url"))

        val longUrl = "any-long-url"
        val shortUrl = service.createShortUrl(longUrl)
        val storedShortUrl = repository.getByShortCode(shortUrl.shortCode)

        assertEquals(ShortUrl(shortCode = "another-code", longUrl = longUrl), shortUrl)
        assertNotNull(storedShortUrl)
        assertEquals(shortUrl, storedShortUrl)
    }

    @Test
    fun `Should fail after max attempts is reached`() {
        val repository = InMemoryShortUrlRepository()
        val shortCodeGenerator = SequenceShortCodeGenerator(generateSequence { "any-code" }.iterator())
        val service = CreateShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

        repository.save(ShortUrl(shortCode = "any-code", longUrl = "any-url"))

        assertFailsWith<IllegalStateException> {
            service.createShortUrl("any-long-url")
        }
    }
}