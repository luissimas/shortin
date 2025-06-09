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
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresShortUrlRepository(
    private val database: Database,
) : ShortUrlRepository {
    object ShortUrls : Table("short_urls") {
        val shortCode = varchar("short_code", length = 10)
        val longUrl = varchar("long_url", length = 255)

        override val primaryKey = PrimaryKey(shortCode)
    }

    // TODO use flyway for migrations
    init {
        transaction(database) {
            SchemaUtils.create(ShortUrls)
        }
    }

    private suspend fun <T> suspendTransaction(block: Transaction.() -> T) =
        newSuspendedTransaction(context = Dispatchers.IO, db = database, statement = block)

    override suspend fun save(url: ShortUrl): Either<DomainError, Unit> =
        suspendTransaction {
            Either
                .catchOrThrow<ExposedSQLException, Unit> {
                    ShortUrls.insert {
                        it[shortCode] = url.shortCode.code
                        it[longUrl] = url.longUrl.url
                    }
                }.mapLeft {
                    val isUniqueConstraintError = it.sqlState == "23505"
                    if (isUniqueConstraintError) {
                        ApplicationError.ShortCodeAlreadyExists
                    } else {
                        throw it
                    }
                }
        }

    override suspend fun getByShortCode(shortCode: ShortCode): Either<DomainError, ShortUrl> =
        suspendTransaction {
            ShortUrls
                .select(
                    ShortUrls.shortCode,
                    ShortUrls.longUrl,
                ).where(ShortUrls.shortCode eq shortCode.code)
                .map {
                    either {
                        val shortCode = ShortCode.create(it[ShortUrls.shortCode]).bind()
                        val longUrl = Url.create(it[ShortUrls.longUrl]).bind()

                        ShortUrl(shortCode = shortCode, longUrl = longUrl)
                    }
                }.singleOrNull() ?: ApplicationError.EntityNotFound.left()
        }
}
