package io.github.luissimas.domain.shorturl

import io.github.luissimas.infrastructure.InMemoryShortUrlRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GetShortUrlServiceTest : FunSpec({
    test("Returns the found short URL") {
        val repository = InMemoryShortUrlRepository()
        val service = GetShortUrlService(repository)

        val shortUrl = ShortUrl(shortCode = "any-short-code", longUrl = "any-long-url")
        repository.save(shortUrl)
        val result = service.getShortUrl(shortUrl.shortCode)

        result shouldBe shortUrl
    }

    test("Returns null if the short URL is not found") {
        val repository = InMemoryShortUrlRepository()
        val service = GetShortUrlService(repository)

        val result = service.getShortUrl("any-short-code")

        result shouldBe null
    }
})