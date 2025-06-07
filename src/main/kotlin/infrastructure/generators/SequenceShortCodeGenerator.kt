package io.github.luissimas.infrastructure.generators

import arrow.core.Either
import arrow.core.right
import io.github.luissimas.core.shorturl.domain.ApplicationError
import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.ports.ShortCodeGenerator

class SequenceShortCodeGenerator(
    private val codes: Iterator<ShortCode>,
) : ShortCodeGenerator {
    override fun generate(): Either<ApplicationError, ShortCode> = codes.next().right()
}
