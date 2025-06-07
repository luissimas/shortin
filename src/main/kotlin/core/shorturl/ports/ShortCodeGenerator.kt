package io.github.luissimas.core.shorturl.ports

import arrow.core.Either
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ShortCode

interface ShortCodeGenerator {
    fun generate(): Either<ApplicationError, ShortCode>
}
