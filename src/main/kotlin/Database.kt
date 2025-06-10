package io.github.luissimas

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.luissimas.infrastructure.persistence.SqlDelightDatabase
import io.ktor.server.application.Application

fun Application.configureDatabase(config: Database): SqlDelightDatabase {
    val dbConfig =
        HikariConfig().apply {
            jdbcUrl = config.url
            username = config.user
            password = config.password
        }
    return HikariDataSource(dbConfig).asJdbcDriver().let {
        SqlDelightDatabase(it)
    }
}
