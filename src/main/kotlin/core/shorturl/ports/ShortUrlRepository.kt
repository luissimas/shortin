package io.github.luissimas.core.shorturl.ports

import arrow.core.Either
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlRepository {
    suspend fun save(url: ShortUrl): Either<DomainError, Unit>

    suspend fun getByShortCode(shortCode: ShortCode): Either<DomainError, ShortUrl>
}
