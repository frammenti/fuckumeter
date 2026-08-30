package schema

import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Test

class UserSchemaTest : SchemaTest() {
    @Test
    suspend fun `updated_at cannot precede created_at`() {
        assertFails {
            insertUser(createdAt = now, updatedAt = before)
        }
    }

    @Test
    suspend fun `deactivated_at cannot precede created_at`() {
        assertFails {
            insertUser(createdAt = now, deactivatedAt = before)
        }
    }

    @Test
    suspend fun `deleted_at requires deactivated_at`() {
        assertFails {
            insertUser(
                createdAt = before,
                deletedAt = now,
            )
        }
    }

    @Test
    suspend fun `deleted_at cannot precede deactivated_at`() {
        assertFails {
            insertUser(
                createdAt = before,
                deactivatedAt = after,
                deletedAt = now,
            )
        }
    }
}
