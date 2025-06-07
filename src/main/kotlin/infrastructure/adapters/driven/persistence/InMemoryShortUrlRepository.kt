package io.github.luissimas.infrastructure.persistence

import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.ShortUrlRepository

class InMemoryShortUrlRepository : ShortUrlRepository {
    private val urls: HashMap<ShortCode, ShortUrl> = hashMapOf()

    override fun save(url: ShortUrl) {
        urls[url.shortCode] = url
    }

    override fun getByShortCode(shortCode: ShortCode): ShortUrl? = urls.get(shortCode)
}
