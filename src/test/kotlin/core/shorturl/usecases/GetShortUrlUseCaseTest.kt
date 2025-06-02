package core.shorturl.usecases

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.usecases.GetShortUrlUseCase
import io.github.luissimas.infrastructure.adapters.driven.persistence.InMemoryShortUrlRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GetShortUrlUseCaseTest :
    FunSpec({
        test("Returns the found short URL") {
            val repository = InMemoryShortUrlRepository()
            val useCase = GetShortUrlUseCase(repository)

            val shortUrl = ShortUrl(shortCode = "any-short-code", longUrl = "any-long-url")
            repository.save(shortUrl)
            val result = useCase(shortUrl.shortCode)

            result shouldBe shortUrl
        }

        test("Returns null if the short URL is not found") {
            val repository = InMemoryShortUrlRepository()
            val useCase = GetShortUrlUseCase(repository)

            val result = useCase("any-short-code")

            result shouldBe null
        }
    })
