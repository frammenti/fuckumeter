package schema

import fixtures.TestFixtures.insertGroup
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Test

class GroupSchemaTest : SchemaTest() {
    @Test
    suspend fun `updated_at cannot precede created_at`() {
        assertFails {
            insertGroup(createdAt = now, updatedAt = before)
        }
    }
}
