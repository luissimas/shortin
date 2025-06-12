package io.github.luissimas.infrastructure.persistence

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import io.github.luissimas.core.shorturl.domain.ApplicationError.EntityNotFound
import io.github.luissimas.core.shorturl.domain.ApplicationError.ShortCodeAlreadyExists
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

    override suspend fun getByShortCode(shortCode: ShortCode): Result<ShortUrl, EntityNotFound> =
        withContext(Dispatchers.IO) {
            queries
                .getByShortCode(
                    shortCode.code,
                ).executeAsOneOrNull()
                .asResultOr { EntityNotFound }
                .map { it.toEntity() }
        }

    override suspend fun save(shortUrl: ShortUrl): Result<ShortUrl, ShortCodeAlreadyExists> =
        withContext(Dispatchers.IO) {
            resultFrom { queries.save(shortUrl.shortCode.code, shortUrl.longUrl.url) }
                .map { shortUrl }
                .mapFailure { exception ->
                    when (exception) {
                        is PSQLException -> {
                            if (exception.sqlState == "23505") {
                                ShortCodeAlreadyExists
                            } else {
                                throw exception
                            }
                        }
                        else -> throw exception
                    }
                }
        }
}

fun Short_urls.toEntity(): ShortUrl {
    val shortCode =
        ShortCode.create(short_code).onFailure {
            error("Retrieved invalid short code from database: '$short_code'")
        }
    val longUrl =
        Url.create(long_url).onFailure {
            error("Retrieved invalid long URL from database: '$long_url'")
        }

    return ShortUrl(shortCode, longUrl)
}
