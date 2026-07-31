package routing

import io.ktor.client.request.post
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test

class UserRoutesTest {

    @Test
    fun testPostUsers() = testApplication {
        application {
            TODO("Add the Ktor module for the test")
        }
        client.post("/users").apply {
            TODO("Please write your test here")
        }
    }
}
