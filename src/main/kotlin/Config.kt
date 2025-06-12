package io.github.luissimas

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addEnvironmentSource
import io.github.luissimas.infrastructure.messaging.KafkaTopic

data class Database(
    val url: String,
    val user: String,
    val password: String,
)

data class Kafka(
    val bootstrapServers: String,
    val topic: KafkaTopic,
)

data class Server(
    val host: String,
    val port: Int,
)

data class Config(
    val database: Database,
    val kafka: Kafka,
    val server: Server,
) {
    companion object {
        /**
         * Load the [Config] from the current environment.
         */
        fun load(): Config =
            ConfigLoaderBuilder
                .default()
                .addEnvironmentSource()
                .withReport()
                .build()
                .loadConfigOrThrow<Config>()
    }
}
