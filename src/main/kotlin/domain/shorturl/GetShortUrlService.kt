package io.github.luissimas.domain.shorturl

class GetShortUrlService(private val repository: ShortUrlRepository) : GetShortUrlUsecase {
    override fun getShortUrl(shortCode: String): ShortUrl? = repository.getByShortCode(shortCode)
}