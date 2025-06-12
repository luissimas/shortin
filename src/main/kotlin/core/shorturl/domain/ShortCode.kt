package io.github.luissimas.core.shorturl.domain

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.luissimas.core.shorturl.domain.DomainError.InvalidShortCode

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
        fun create(value: String?): Result<ShortCode, InvalidShortCode> =
            when {
                value.isNullOrBlank() -> Failure(InvalidShortCode)
                value.any { it !in VALID_CHARS } -> Failure(InvalidShortCode)
                else -> Success(ShortCode(value))
            }
    }
}
