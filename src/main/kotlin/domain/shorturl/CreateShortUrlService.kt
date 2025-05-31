package io.github.luissimas.domain.shorturl

class CreateShortUrlService(
    private val repository: ShortUrlRepository,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val maxAttempts: Int = 5,
) : CreateShortUrlUsecase {
    override fun createShortUrl(longUrl: String): ShortUrl {
        repeat(maxAttempts) {
            val shortCode = shortCodeGenerator.generate()
            val existingShortUrl = repository.getByShortCode(shortCode)

            if (existingShortUrl == null) {
                val url = ShortUrl(shortCode = shortCode, longUrl = longUrl)
                repository.save(url)

                return url
            }
        }

        throw IllegalStateException("Unable to generate unique short code after $maxAttempts attempts")
    }
}