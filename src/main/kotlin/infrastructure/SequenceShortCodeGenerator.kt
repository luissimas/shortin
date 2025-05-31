package io.github.luissimas.infrastructure

import io.github.luissimas.domain.shorturl.ShortCodeGenerator

class SequenceShortCodeGenerator(private val codes: Iterator<String>) : ShortCodeGenerator {
    override fun generate(): String = codes.next()
}