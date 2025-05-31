package io.github.luissimas.domain.shorturl

interface CreateShortUrlUsecase {
    fun createShortUrl(longUrl: String): ShortUrl
}