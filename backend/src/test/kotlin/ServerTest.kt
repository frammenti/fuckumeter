import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        configure()
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }
}
