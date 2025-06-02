package core.shorturl.usecases

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.usecases.CreateShortUrlUseCase
import io.github.luissimas.infrastructure.generators.SequenceShortCodeGenerator
import io.github.luissimas.infrastructure.persistence.InMemoryShortUrlRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class CreateShortUrlUseCaseTest :
    FunSpec({
        test("Should create a short URL and save it on the repository") {
            val repository = InMemoryShortUrlRepository()
            val shortCodeGenerator = SequenceShortCodeGenerator(listOf("any-code").iterator())
            val useCase = CreateShortUrlUseCase(repository = repository, shortCodeGenerator = shortCodeGenerator)

            val longUrl = "any-long-url"
            val shortUrl = useCase.createShortUrl(longUrl)
            val storedShortUrl = repository.getByShortCode(shortUrl.shortCode)

            shortUrl shouldBe ShortUrl(shortCode = "any-code", longUrl = longUrl)
            storedShortUrl shouldBe shortUrl
        }

        test("Should retry short code creation on short code conflicts") {
            val repository = InMemoryShortUrlRepository()
            val shortCodeGenerator = SequenceShortCodeGenerator(listOf("any-code", "another-code").iterator())
            val useCase = CreateShortUrlUseCase(repository = repository, shortCodeGenerator = shortCodeGenerator)

            repository.save(ShortUrl(shortCode = "any-code", longUrl = "any-url"))

            val longUrl = "any-long-url"
            val shortUrl = useCase.createShortUrl(longUrl)
            val storedShortUrl = repository.getByShortCode(shortUrl.shortCode)

            shortUrl shouldBeEqual ShortUrl(shortCode = "another-code", longUrl = longUrl)
            storedShortUrl shouldBe shortUrl
        }

        test("Should fail after max attempts is reached") {
            val repository = InMemoryShortUrlRepository()
            val shortCodeGenerator = SequenceShortCodeGenerator(generateSequence { "any-code" }.iterator())
            val useCase = CreateShortUrlUseCase(repository = repository, shortCodeGenerator = shortCodeGenerator)

            repository.save(ShortUrl(shortCode = "any-code", longUrl = "any-url"))

            shouldThrow<IllegalStateException> {
                useCase.createShortUrl("any-long-url")
            }
        }
    })
