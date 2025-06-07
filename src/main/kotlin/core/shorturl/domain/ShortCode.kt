package io.github.luissimas.core.shorturl.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull

/**
 * A valid domain short code.
 */
@JvmInline
value class ShortCode private constructor(
    val code: String,
) {
    companion object {
        private const val VALID_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

        /**
         * Constructs a new valid [ShortCode].
         */
        fun create(value: String?): Either<ValidationError, ShortCode> =
            either {
                ensureNotNull(value) { ValidationError.MissingValue }
                ensure(value.all { VALID_CHARS.contains(it) }) { ValidationError.InvalidShortCode }
                ShortCode(value)
            }
    }
}
