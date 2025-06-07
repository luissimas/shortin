package core.shorturl.services

import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.services.ShortUrlService
import io.github.luissimas.infrastructure.generators.SequenceShortCodeGenerator
import io.github.luissimas.infrastructure.persistence.InMemoryShortUrlRepository
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ShortUrlServiceTest :
    DescribeSpec({
        describe("Create short URL") {
            it("Should create a short URL and save it on the repository") {
                val repository = InMemoryShortUrlRepository()
                val shortCode = ShortCode.create("anycode").shouldBeRight()
                val shortCodeGenerator = SequenceShortCodeGenerator(listOf(shortCode).iterator())
                val service = ShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

                val longUrl = Url.create("https://any-long-url").shouldBeRight()
                val shortUrl = service.createShortUrl(longUrl).shouldBeRight()
                val storedShortUrl = repository.getByShortCode(shortUrl.shortCode).shouldBeRight()

                shortUrl shouldBe ShortUrl(shortCode = shortCode, longUrl = longUrl)
                storedShortUrl shouldBe shortUrl
            }

            it("Should retry short code creation on short code conflicts") {
                val repository = InMemoryShortUrlRepository()
                val existingShortCode = ShortCode.create("anycode").shouldBeRight()
                val availableShortCode = ShortCode.create("anothercode").shouldBeRight()
                val existingLongUrl = Url.create("http://existing-long-url").shouldBeRight()
                val shortCodeGenerator =
                    SequenceShortCodeGenerator(
                        listOf(existingShortCode, availableShortCode).iterator(),
                    )
                val service = ShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

                repository.save(ShortUrl(shortCode = existingShortCode, longUrl = existingLongUrl))

                val longUrl = Url.create("http://any-long-url").shouldBeRight()
                val shortUrl = service.createShortUrl(longUrl).shouldBeRight()
                val storedShortUrl = repository.getByShortCode(shortUrl.shortCode).shouldBeRight()

                storedShortUrl shouldBe shortUrl
                shortUrl shouldBe ShortUrl(shortCode = availableShortCode, longUrl = longUrl)
            }

            it("Should fail after max attempts is reached") {
                val repository = InMemoryShortUrlRepository()
                val existingShortCode = ShortCode.create("anycode").shouldBeRight()
                val shortCodeGenerator = SequenceShortCodeGenerator(generateSequence { existingShortCode }.iterator())
                val service = ShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)
                val longUrl = Url.create("https://any-long-url").shouldBeRight()

                repository.save(ShortUrl(shortCode = existingShortCode, longUrl = longUrl))

                val result = service.createShortUrl(longUrl)

                result shouldBeLeft ApplicationError.MaxAttemptsReached
            }
        }

        describe("Get short URL") {
            it("Returns the found short URL") {
                val repository = InMemoryShortUrlRepository()
                val shortCode = ShortCode.create("anycode").shouldBeRight()
                val longUrl = Url.create("https://any-long-url").shouldBeRight()
                val shortCodeGenerator = SequenceShortCodeGenerator(listOf(shortCode).iterator())
                val service = ShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

                val shortUrl = ShortUrl(shortCode = shortCode, longUrl = longUrl)
                repository.save(shortUrl)
                val result = service.getShortUrl(shortCode)

                result shouldBeRight shortUrl
            }

            it("Returns null if the short URL is not found") {
                val repository = InMemoryShortUrlRepository()
                val shortCode = ShortCode.create("anycode").shouldBeRight()
                val shortCodeGenerator = SequenceShortCodeGenerator(listOf(shortCode).iterator())
                val service = ShortUrlService(repository = repository, shortCodeGenerator = shortCodeGenerator)

                val result = service.getShortUrl(shortCode)

                result shouldBeLeft ApplicationError.EntityNotFound
            }
        }
    })
