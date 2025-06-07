package io.github.luissimas.core.shorturl.ports

import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlRepository {
    fun save(url: ShortUrl)

    fun getByShortCode(shortCode: ShortCode): ShortUrl?
}
