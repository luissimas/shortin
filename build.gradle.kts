plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.flyway)
    alias(libs.plugins.sqldelight)
}

group = "io.github.luissimas"
version = "0.0.1"

application {
    mainClass = "io.github.luissimas.KtorApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.postgresql)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.hoplite.core)
    implementation(libs.sqldelight.jdbc)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.hikari)
    implementation(libs.kotlin.kafka)
    implementation(libs.result4k)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotest.runner.junit)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.assertions.ktor)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.postgres)
    testImplementation(libs.result4k.kotest)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val integrationTest by sourceSets.creating {
    java.srcDir("src/integrationTest/kotlin")
    resources.srcDir("src/integrationTest/resources")

    compileClasspath += sourceSets["main"].output + configurations["testCompileClasspath"]
    runtimeClasspath += output + compileClasspath
}

configurations["integrationTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"

    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath

    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.named("check") { dependsOn("integrationTest") }

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:${libs.versions.flyway.version.get()}")
    }
}

flyway {
    url = System.getenv("DATABASE__URL")
    user = System.getenv("DATABASE__USER")
    password = System.getenv("DATABASE__PASSWORD")
}

sqldelight {
    databases {
        create("SqlDelightDatabase") {
            packageName.set("io.github.luissimas.infrastructure.persistence")
            dialect("app.cash.sqldelight:postgresql-dialect:${libs.versions.sqldelight.version.get()}")
            deriveSchemaFromMigrations.set(true)
            verifyMigrations.set(true)
            migrationOutputDirectory.set(file("src/main/resources/db/migration"))
            migrationOutputFileFormat = ".sql"
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateMainSqlDelightDatabaseMigrations")
}

tasks.named("processResources") {
    dependsOn("generateMainSqlDelightDatabaseMigrations")
}
