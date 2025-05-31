package io.github.luissimas

import io.kotest.core.spec.style.FunSpec
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.assertEquals

class ApplicationTest : FunSpec({
    test("Root path") {
        testApplication {
            application { module() }

            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }
    }
})