package schema

import fixtures.TestFixtures.insertGroup
import fixtures.TestFixtures.insertMembership
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class MembershipSchemaTest : SchemaTest() {
    @Test
    suspend fun `must have joined if leaving`() {
        assertFails {
            insertMembership(joinedAt = null, leftAt = now)
        }
    }

    @Test
    suspend fun `cannot leave before joining`() {
        assertFails {
            insertMembership(joinedAt = now, leftAt = before)
        }
    }

    @Test
    suspend fun `cannot join twice`() {
        val user = insertUser()
        val group = insertGroup()

        insertMembership(
            userId = user,
            groupId = group,
        )

        assertFails {
            insertMembership(
                userId = user,
                groupId = group,
            )
        }
    }

    @Test
    suspend fun `can join again after leaving`() {
        val user = insertUser()
        val group = insertGroup()

        insertMembership(
            userId = user,
            groupId = group,
            joinedAt = now,
            leftAt = after,
        )

        assertDoesNotThrow {
            insertMembership(
                userId = user,
                groupId = group,
            )
        }
    }
}
