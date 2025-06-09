package io.github.luissimas

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addEnvironmentSource

data class Database(
    val url: String,
    val user: String,
    val password: String,
)

data class Server(
    val host: String,
    val port: Int,
)

data class Config(
    val database: Database,
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
