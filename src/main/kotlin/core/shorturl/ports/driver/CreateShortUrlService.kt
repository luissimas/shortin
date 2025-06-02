package io.github.luissimas.core.shorturl.ports.driver

import io.github.luissimas.core.shorturl.domain.ShortUrl

interface CreateShortUrlService {
    operator fun invoke(longUrl: String): ShortUrl
}
