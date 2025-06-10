package presentation.rest

import io.github.luissimas.Config
import io.github.luissimas.Database
import io.github.luissimas.Server
import io.github.luissimas.module
import io.github.luissimas.presentation.rest.CreateShortUrlRequest
import io.github.luissimas.presentation.rest.CreateShortUrlResponse
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.ExperimentalSerializationApi
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

@OptIn(ExperimentalSerializationApi::class)
class ShortUrlRoutesTest :
    FunSpec({
        val postgresContainer =
            PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
                withDatabaseName("shortin")
                withUsername("postgres")
                withPassword("postgres")
            }

        beforeSpec {
            postgresContainer.start()
            Flyway
                .configure()
                .dataSource(
                    postgresContainer.jdbcUrl,
                    postgresContainer.username,
                    postgresContainer.password,
                ).load()
                .migrate()
        }

        afterSpec {
            postgresContainer.stop()
        }

        test("Should create short URL and redirect to it on query") {
            testApplication {
                val database =
                    Database(
                        url = postgresContainer.jdbcUrl,
                        user = postgresContainer.username,
                        password = postgresContainer.password,
                    )
                val server = Server(host = "localhost", port = 8080)
                val config = Config(database = database, server = server)
                application { module(config) }
                val client =
                    createClient {
                        install(ContentNegotiation) {
                            json()
                        }
                        followRedirects = false
                    }

                val longUrl = "https://www.google.com"
                val body = CreateShortUrlRequest(longUrl)
                val createResponse =
                    client.post("/urls") {
                        header(HttpHeaders.ContentType, ContentType.Application.Json)
                        setBody(body)
                    }
                createResponse shouldHaveStatus HttpStatusCode.Created
                val responseBody = createResponse.body<CreateShortUrlResponse>()
                responseBody.longUrl shouldBe longUrl

                val redirectResponse = client.get("/r/${responseBody.shortCode}")
                redirectResponse shouldHaveStatus HttpStatusCode.Found
                redirectResponse.headers["Location"] shouldBe longUrl
            }
        }
    })
