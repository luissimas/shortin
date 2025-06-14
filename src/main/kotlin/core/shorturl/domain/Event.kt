package io.github.luissimas.core.shorturl.domain

sealed class Event {
    data class ShortUrlAccessed(
        val shortUrl: ShortUrl,
    ) : Event()
}
