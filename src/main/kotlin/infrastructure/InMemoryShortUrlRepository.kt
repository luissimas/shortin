package io.github.luissimas.infrastructure

import io.github.luissimas.domain.shorturl.ShortUrl
import io.github.luissimas.domain.shorturl.ShortUrlRepository

class InMemoryShortUrlRepository : ShortUrlRepository {
    private val urls: HashMap<String, ShortUrl> = hashMapOf()

    override fun save(url: ShortUrl) {
        urls[url.shortCode] = url
    }

    override fun getByShortCode(shortCode: String): ShortUrl? = urls.get(shortCode)
}