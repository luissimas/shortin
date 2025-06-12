package io.github.luissimas.infrastructure.messaging

import io.github.luissimas.core.shorturl.domain.Event
import io.github.luissimas.core.shorturl.domain.Event.ShortUrlAccessed
import io.github.luissimas.core.shorturl.domain.EventPublisher
import io.github.nomisRev.kafka.publisher.KafkaPublisher
import io.github.nomisRev.kafka.publisher.PublisherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.ProducerRecord

@JvmInline
value class KafkaTopic(
    val topic: String,
)

class KafkaEventPublisher(
    val topic: KafkaTopic,
    publisherSettings: PublisherSettings<String, String>,
) : EventPublisher {
    val publisher = KafkaPublisher(publisherSettings)

    override suspend fun publish(event: Event) {
        withContext(Dispatchers.IO) {
            publisher.publishScope {
                publish(ProducerRecord(topic.topic, "short-url-accessed", event.serialize()))
            }
        }
    }

    fun Event.serialize() =
        when (this) {
            is ShortUrlAccessed -> this.shortUrl.shortCode.code
        }
}
