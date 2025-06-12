package io.github.luissimas.core.shorturl.domain

sealed interface DomainError {
    object InvalidShortCode : DomainError

    object InvalidUrl : DomainError
}

sealed interface ApplicationError {
    object EntityNotFound : ApplicationError

    object CouldNotAllocateShortCode : ApplicationError

    object ShortCodeAlreadyExists : ApplicationError
}
