package io.github.luissimas.core.shorturl.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.net.URL

/**
 * A valid domain URL.
 */
@JvmInline
value class Url private constructor(
    val url: String,
) {
    companion object {
        /**
         * Constructs a new valid [Url].
         */
        fun create(value: String?): Either<ValidationError, Url> =
            runCatching { URL(value) }.fold(
                { Url(it.toString()).right() },
                { ValidationError.InvalidUrl.left() },
            )
    }
}
