package io.github.luissimas.domain.shorturl

interface ShortUrlRepository {
    fun save(url: ShortUrl)
    fun getByShortCode(shortCode: String): ShortUrl?
}