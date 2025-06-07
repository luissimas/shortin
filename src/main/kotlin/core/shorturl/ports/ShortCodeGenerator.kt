package io.github.luissimas.core.shorturl.ports

import io.github.luissimas.core.shorturl.domain.ShortCode

interface ShortCodeGenerator {
    fun generate(): ShortCode
}
