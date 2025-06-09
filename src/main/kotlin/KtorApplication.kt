package io.github.luissimas

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

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
    configureRouting(database)
}
