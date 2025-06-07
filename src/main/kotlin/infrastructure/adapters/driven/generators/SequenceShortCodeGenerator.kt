package io.github.luissimas.infrastructure.adapters.driven.generators

import io.github.luissimas.core.shorturl.domain.ShortCode
import io.github.luissimas.core.shorturl.ports.ShortCodeGenerator

class SequenceShortCodeGenerator(
    private val codes: Iterator<ShortCode>,
) : ShortCodeGenerator {
    override fun generate(): ShortCode = codes.next()
}
