package io.github.luissimas.infrastructure.persistence

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.DomainError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.domain.Url
import io.github.luissimas.core.shorturl.ports.ShortUrlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import migrations.Short_urls
import org.postgresql.util.PSQLException

class SqlDelightShortUrlRepository(
    private val database: SqlDelightDatabase,
) : ShortUrlRepository {
    private val queries = database.shortUrlsQueries

    override suspend fun getByShortCode(shortCode: ShortCode): Either<DomainError, ShortUrl> =
        withContext(Dispatchers.IO) {
            queries.getByShortCode(shortCode.code).executeAsOneOrNull().let {
                it?.toEntity() ?: ApplicationError.EntityNotFound.left()
            }
        }

    override suspend fun save(url: ShortUrl): Either<DomainError, Unit> =
        withContext(Dispatchers.IO) {
            Either
                .catchOrThrow<PSQLException, Unit> {
                    queries.save(url.shortCode.code, url.longUrl.url)
                }.mapLeft {
                    val isUniqueConstraintError = it.sqlState == "23505"
                    if (isUniqueConstraintError) {
                        ApplicationError.ShortCodeAlreadyExists
                    } else {
                        throw it
                    }
                }
        }
}

fun Short_urls.toEntity() =
    either {
        val shortCode = ShortCode.create(short_code).bind()
        val longUrl = Url.create(long_url).bind()

        ShortUrl(shortCode, longUrl)
    }
