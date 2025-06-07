package io.github.luissimas.core.shorturl.domain

sealed interface DomainError

sealed class ValidationError : DomainError {
    object MissingValue : ValidationError()

    object InvalidShortCode : ValidationError()

    object InvalidUrl : ValidationError()
}

sealed class ApplicationError : DomainError {
    object EntityNotFound : ApplicationError()

    object MaxAttemptsReached : ApplicationError()

    object ShortCodeAlreadyExists : ApplicationError()
}
