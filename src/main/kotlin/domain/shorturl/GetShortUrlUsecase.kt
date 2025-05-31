package io.github.luissimas.domain.shorturl

interface GetShortUrlUsecase {
    fun getShortUrl(shortCode: String): ShortUrl?
}