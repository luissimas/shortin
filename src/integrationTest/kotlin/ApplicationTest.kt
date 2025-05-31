package io.github.luissimas

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest : FunSpec({
    test("Root path") {
        testApplication {
            application { module() }

            val response = client.get("/")
            response shouldHaveStatus HttpStatusCode.OK
        }
    }
})
