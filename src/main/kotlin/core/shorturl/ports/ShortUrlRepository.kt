package io.github.luissimas.core.shorturl.ports

import arrow.core.Either
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl

interface ShortUrlRepository {
    fun save(url: ShortUrl): Either<ApplicationError, Unit>

    fun getByShortCode(shortCode: ShortCode): Either<ApplicationError, ShortUrl>
}
