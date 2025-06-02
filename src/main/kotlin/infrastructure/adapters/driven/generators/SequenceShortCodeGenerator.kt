package io.github.luissimas.infrastructure.adapters.driven.generators

import io.github.luissimas.core.shorturl.ports.driven.ShortCodeGenerator

class SequenceShortCodeGenerator(
    private val codes: Iterator<String>,
) : ShortCodeGenerator {
    override fun generate(): String = codes.next()
}
