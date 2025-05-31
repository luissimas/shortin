package io.github.luissimas.domain.shorturl

import io.github.luissimas.infrastructure.InMemoryShortUrlRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetShortUrlServiceTest {
    @Test
    fun `Returns the found short URL`() {
        val repository = InMemoryShortUrlRepository()
        val service = GetShortUrlService(repository)

        val shortUrl = ShortUrl(shortCode = "any-short-code", longUrl = "any-long-url")
        repository.save(shortUrl)
        val result = service.getShortUrl(shortUrl.shortCode)

        assertEquals(result, shortUrl)
    }

    @Test
    fun `Returns null if the short URL is not found`() {
        val repository = InMemoryShortUrlRepository()
        val service = GetShortUrlService(repository)

        val result = service.getShortUrl("any-short-code")

        assertNull(result)
    }
}