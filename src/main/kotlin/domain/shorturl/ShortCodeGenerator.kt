package io.github.luissimas.domain.shorturl

interface ShortCodeGenerator {
    fun generate(): String
}