package presentation.rest

import io.github.luissimas.Config
import io.github.luissimas.Database
import io.github.luissimas.Server
import io.github.luissimas.module
import io.github.luissimas.presentation.rest.CreateShortUrlRequest
import io.github.luissimas.presentation.rest.CreateShortUrlResponse
import io.github.luissimas.presentation.rest.HttpError
import io.kotest.assertions.ktor.client.shouldHaveHeader
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.DescribeSpec
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
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

class ShortUrlRoutesTest :
    DescribeSpec({
        val postgresContainer =
            PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
                withDatabaseName("shortin")
                withUsername("postgres")
                withPassword("postgres")
            }

        fun config() =
            Config(
                database =
                    Database(
                        url = postgresContainer.jdbcUrl,
                        user = postgresContainer.username,
                        password = postgresContainer.password,
                    ),
                server = Server(host = "localhost", port = 8080),
            )

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

        describe("Create short URL") {
            it("Should create short URL and redirect to it on query") {
                testApplication {
                    application { module(config()) }
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
                    createResponse.shouldHaveHeader(
                        HttpHeaders.ContentType,
                        "${ContentType.Application.Json}; charset=UTF-8",
                    )
                    val responseBody = createResponse.body<CreateShortUrlResponse>()
                    responseBody.longUrl shouldBe longUrl

                    val redirectResponse = client.get("/r/${responseBody.shortCode}")
                    redirectResponse shouldHaveStatus HttpStatusCode.Found
                    redirectResponse.shouldHaveHeader(HttpHeaders.Location, longUrl)
                }
            }

            it("Should return bad request for invalid urls") {
                testApplication {
                    application { module(config()) }
                    val client =
                        createClient {
                            install(ContentNegotiation) {
                                json()
                            }
                            followRedirects = false
                        }

                    val longUrl = "invalid-url"
                    val body = CreateShortUrlRequest(longUrl)
                    val response =
                        client.post("/urls") {
                            header(HttpHeaders.ContentType, ContentType.Application.Json)
                            setBody(body)
                        }
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.shouldHaveHeader(
                        HttpHeaders.ContentType,
                        "${ContentType.Application.Json}; charset=UTF-8",
                    )
                    response.body<HttpError>() shouldBe HttpError("Invalid URL")
                }
            }
        }

        describe("Get short URL") {
            it("Should return not found for non existing short codes") {
                testApplication {
                    application { module(config()) }
                    val client =
                        createClient {
                            install(ContentNegotiation) {
                                json()
                            }
                            followRedirects = false
                        }

                    val response = client.get("/r/idontexist")
                    response shouldHaveStatus HttpStatusCode.NotFound
                    response.shouldHaveHeader(
                        HttpHeaders.ContentType,
                        "${ContentType.Application.Json}; charset=UTF-8",
                    )
                    response.body<HttpError>() shouldBe HttpError("Not found")
                }
            }

            it("Should return bad request for invalid short codes") {
                testApplication {
                    application { module(config()) }
                    val client =
                        createClient {
                            install(ContentNegotiation) {
                                json()
                            }
                            followRedirects = false
                        }

                    val response = client.get("/r/@#$*-")
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.shouldHaveHeader(
                        HttpHeaders.ContentType,
                        "${ContentType.Application.Json}; charset=UTF-8",
                    )
                    response.body<HttpError>() shouldBe HttpError("Invalid short code")
                }
            }
        }
    })
