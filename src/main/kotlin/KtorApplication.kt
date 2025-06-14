package io.github.luissimas

import io.github.nomisRev.kafka.publisher.PublisherSettings
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.kafka.common.serialization.StringSerializer

fun main() {
    val config = Config.load()
    embeddedServer(Netty, port = config.server.port, host = config.server.host, module = {
        module(config)
    }).start(wait = true)
}

fun Application.module(config: Config) {
    configureMonitoring()
    configureSerialization()
    configureHTTP()
    val database = configureDatabase(config.database)
    val publisherSettings =
        PublisherSettings(
            config.kafka.bootstrapServers,
            keySerializer = StringSerializer(),
            valueSerializer = StringSerializer(),
        )
    configureRouting(database = database, kafkaTopic = config.kafka.topic, kafkaPublisherSettings = publisherSettings)
}
