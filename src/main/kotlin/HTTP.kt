package io.github.luissimas

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.configureHTTP() {
    install(DefaultHeaders)
}
