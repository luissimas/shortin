package io.github.luissimas.core.shorturl.domain

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import io.github.luissimas.core.shorturl.domain.DomainError.InvalidUrl
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
        fun create(value: String?): Result<Url, InvalidUrl> =
            resultFrom { URL(value) }
                .map { Url(it.toString()) }
                .mapFailure { InvalidUrl }
    }
}
