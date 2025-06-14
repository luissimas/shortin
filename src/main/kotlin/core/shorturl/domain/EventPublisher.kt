package io.github.luissimas.core.shorturl.domain

interface EventPublisher {
    /**
     * Opted for a Unit result instead of `Either<DomainError, Unit>`
     * because I don't think there's any error in emitting an event
     * that is relevant for the domain code. If that's the case, then
     * any error here is an exception and should not be handled by
     * the domain code. It only makes sense to model the errors in
     * the * type system if we're going to do something about them.
     * Is this true?
     */
    suspend fun publish(event: Event): Unit
}
