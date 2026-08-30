package schema

import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestDatabase
import java.sql.SQLException
import java.time.Instant
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class SchemaTest {
    protected val now: Instant = now()
    protected val before: Instant = now.minusSeconds(1)
    protected val after: Instant = now.plusSeconds(1)

    protected suspend fun <T> assertFails(block: suspend () -> T) =
        assertThrows<SQLException> { block() }

    @BeforeEach
    suspend fun setUp() {
        TestDatabase.truncate()
    }

    @AfterAll
    suspend fun cleanUp() {
        TestDatabase.truncate()
    }
}
