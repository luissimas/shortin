package io.github.luissimas.infrastructure.adapters.driver.rest

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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
class ShortUrlRoutesTest :
    FunSpec({
        test("Should create short URL and redirect to it on query") {
            testApplication {
                application { module() }
                val client =
                    createClient {
                        install(ContentNegotiation) {
                            json(Json { namingStrategy = JsonNamingStrategy.SnakeCase })
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
