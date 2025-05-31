package io.github.luissimas.domain.shorturl

/**
 * `ShortUrl` associates a `shortCode` to a `longUrl`.
 */
data class ShortUrl(val shortCode: String, val longUrl: String)