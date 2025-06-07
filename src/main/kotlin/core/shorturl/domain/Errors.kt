package io.github.luissimas.core.shorturl.domain

sealed interface DomainError

data class ValidationError(
    val message: String,
) : DomainError
