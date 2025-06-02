package io.github.luissimas.core.shorturl.domain

/**
 * `ShortUrl` associates a `shortCode` to a `longUrl`.
 */
data class ShortUrl(
    val shortCode: String,
    val longUrl: String,
)
