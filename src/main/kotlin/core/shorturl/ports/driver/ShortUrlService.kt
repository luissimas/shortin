package io.github.luissimas.core.shorturl.ports.driver

import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlService {
    fun createShortUrl(longUrl: String): ShortUrl
}
