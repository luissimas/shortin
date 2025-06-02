package io.github.luissimas.infrastructure.adapters.driven.persistence

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.driven.ShortUrlRepository

class InMemoryShortUrlRepository : ShortUrlRepository {
    private val urls: HashMap<String, ShortUrl> = hashMapOf()

    override fun save(url: ShortUrl) {
        urls[url.shortCode] = url
    }

    override fun getByShortCode(shortCode: String): ShortUrl? = urls[shortCode]
}
