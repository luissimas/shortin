package io.github.luissimas.core.shorturl.domain

/**
 * A domain short URL.
 */
data class ShortUrl(
    val shortCode: ShortCode,
    val longUrl: Url,
)
