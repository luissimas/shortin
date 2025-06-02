package io.github.luissimas.core.shorturl.ports.driver

import io.github.luissimas.core.shorturl.domain.ShortUrl

interface GetShortUrlService {
    fun getShortUrl(shortCode: String): ShortUrl?
}
