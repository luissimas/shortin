package io.github.luissimas.infrastructure.persistence

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
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
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ExposedShortUrlRepository(
    private val database: Database,
) : ShortUrlRepository {
    object ShortUrls : Table("short_urls") {
        val shortCode = varchar("short_code", length = 10)
        val longUrl = varchar("long_url", length = 255)

        override val primaryKey = PrimaryKey(shortCode)
    }

    private suspend fun <T> suspendTransaction(block: Transaction.() -> T) =
        newSuspendedTransaction(context = Dispatchers.IO, db = database, statement = block)

    override suspend fun save(shortUrl: ShortUrl): Result<ShortUrl, ShortCodeAlreadyExists> =
        suspendTransaction {
            resultFrom {
                ShortUrls.insert {
                    it[shortCode] = shortUrl.shortCode.code
                    it[longUrl] = shortUrl.longUrl.url
                }
            }.map { shortUrl }
                .mapFailure { exception ->
                    when (exception) {
                        is ExposedSQLException -> {
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

    override suspend fun getByShortCode(shortCode: ShortCode): Result<ShortUrl, EntityNotFound> =
        suspendTransaction {
            ShortUrls
                .select(
                    ShortUrls.shortCode,
                    ShortUrls.longUrl,
                ).where(ShortUrls.shortCode eq shortCode.code)
                .map {
                    val storedShortCode = it[ShortUrls.shortCode]
                    val shortCode =
                        ShortCode.create(storedShortCode).onFailure {
                            error(
                                "Retrieved invalid short code from database: '$storedShortCode'",
                            )
                        }
                    val storedLongUrl = it[ShortUrls.longUrl]
                    val longUrl =
                        Url.create(storedLongUrl).onFailure {
                            error("Retrieved invalid long URL from database: '$storedLongUrl'")
                        }

                    Success(ShortUrl(shortCode = shortCode, longUrl = longUrl))
                }.singleOrNull() ?: Failure(EntityNotFound)
        }
}
