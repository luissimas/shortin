package io.github.luissimas.core.shorturl.ports

import dev.forkhandles.result4k.Result
import io.github.luissimas.core.shorturl.domain.ApplicationError.EntityNotFound
import io.github.luissimas.core.shorturl.domain.ApplicationError.ShortCodeAlreadyExists
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlRepository {
    suspend fun save(shortUrl: ShortUrl): Result<ShortUrl, ShortCodeAlreadyExists>

    suspend fun getByShortCode(shortCode: ShortCode): Result<ShortUrl, EntityNotFound>
}
