package io.github.luissimas.infrastructure.persistence

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import io.github.luissimas.core.shorturl.domain.ApplicationError.EntityNotFound
import io.github.luissimas.core.shorturl.domain.ApplicationError.ShortCodeAlreadyExists
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.ShortUrlRepository

class InMemoryShortUrlRepository : ShortUrlRepository {
    private val urls: HashMap<ShortCode, ShortUrl> = hashMapOf()

    override suspend fun save(shortUrl: ShortUrl): Result<ShortUrl, ShortCodeAlreadyExists> =
        when (urls[shortUrl.shortCode]) {
            null -> {
                urls[shortUrl.shortCode] = shortUrl
                Success(shortUrl)
            }
            else -> Failure(ShortCodeAlreadyExists)
        }

    override suspend fun getByShortCode(shortCode: ShortCode): Result<ShortUrl, EntityNotFound> =
        urls.get(shortCode).asResultOr { EntityNotFound }
}
