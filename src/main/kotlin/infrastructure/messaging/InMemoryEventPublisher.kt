package io.github.luissimas.infrastructure.messaging

import io.github.luissimas.core.shorturl.domain.Event
import io.github.luissimas.core.shorturl.domain.EventPublisher

class InMemoryEventPublisher : EventPublisher {
    val events = mutableListOf<Event>()

    override suspend fun publish(event: Event) {
        events.add(event)
    }
}
