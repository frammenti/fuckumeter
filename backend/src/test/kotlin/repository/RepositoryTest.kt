package repository

import dev.frammenti.fuckumeter.db.Database
import fixtures.TestDatabase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class RepositoryTest {
    val database: Database = TestDatabase.database

    @BeforeEach
    fun setUp() {
        TestDatabase.truncate()
    }

    @AfterAll
    fun cleanUp() {
        TestDatabase.truncate()
    }
}
