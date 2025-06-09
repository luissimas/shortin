package io.github.luissimas.infrastructure.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.ShortUrlRepository

class InMemoryShortUrlRepository : ShortUrlRepository {
    private val urls: HashMap<ShortCode, ShortUrl> = hashMapOf()

    override suspend fun save(url: ShortUrl): Either<DomainError, Unit> {
        val existingUrl = urls[url.shortCode]
        if (existingUrl != null) {
            return ApplicationError.ShortCodeAlreadyExists.left()
        }

        urls[url.shortCode] = url
        return Unit.right()
    }

    override suspend fun getByShortCode(shortCode: ShortCode): Either<DomainError, ShortUrl> =
        urls.get(shortCode)?.right() ?: ApplicationError.EntityNotFound.left()
}
