package io.github.luissimas.core.shorturl.ports.driven

import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlRepository {
    fun save(url: ShortUrl)

    fun getByShortCode(shortCode: String): ShortUrl?
}
