package io.github.luissimas

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase(config: io.github.luissimas.Database): Database =
    Database.connect(
        url = config.url,
        driver = "org.postgresql.Driver",
        user = config.user,
        password = config.password,
    )
