package schema

import fixtures.TestDatabase.database
import fixtures.TestFixtures.insertRelationship
import fixtures.TestFixtures.insertRelationshipPair
import fixtures.TestFixtures.insertUser
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class RelationshipSchemaTest : SchemaTest() {
    @Test
    suspend fun `relationship cannot exist outside of a pair`() {
        val ids = UUID.randomUUID() to UUID.randomUUID()
        val users = insertUser() to insertUser()

        assertFails {
            insertRelationship(
                id = ids.first,
                userId = users.first,
                partnerId = users.second,
                otherRelationshipId = ids.second,
            )
        }
    }

    @Test
    suspend fun `other_relationship_id cannot be equal to id`() {
        val ids = UUID.randomUUID() to UUID.randomUUID()
        val users = insertUser() to insertUser()

        assertFails {
            database.transaction {
                insertRelationship(
                    id = ids.first,
                    userId = users.first,
                    partnerId = users.second,
                    otherRelationshipId = ids.first, // error
                )
                insertRelationship(
                    id = ids.second,
                    userId = users.second,
                    partnerId = users.first,
                    otherRelationshipId = ids.first,
                )
            }
        }
    }

    @Test
    suspend fun `partner_id cannot be equal to user_id`() {
        val ids = UUID.randomUUID() to UUID.randomUUID()
        val users = insertUser() to insertUser()

        assertFails {
            database.transaction {
                insertRelationship(
                    id = ids.first,
                    userId = users.first,
                    partnerId = users.first, // error
                    otherRelationshipId = ids.second,
                )
                insertRelationship(
                    id = ids.second,
                    userId = users.second,
                    partnerId = users.first,
                    otherRelationshipId = ids.first,
                )
            }
        }
    }

    @Test
    suspend fun `notification_threshold must be between 0 and 100`() {
        assertDoesNotThrow {
            insertRelationshipPair(notificationThreshold = 0)
        }
        assertDoesNotThrow {
            insertRelationshipPair(notificationThreshold = 100)
        }
        assertFails {
            insertRelationshipPair(notificationThreshold = -1)
        }
        assertFails {
            insertRelationshipPair(notificationThreshold = 101)
        }
    }

    @Test
    suspend fun `there cannot be more than one active relationship`() {
        val users = insertUser() to insertUser()

        insertRelationshipPair(users)

        assertFails {
            insertRelationshipPair(users)
        }
    }

    @Test
    suspend fun `there can be another relationship if the previous was deleted`() {
        val users = insertUser() to insertUser()

        insertRelationshipPair(users, createdAt = before, deletedAt = now)

        assertDoesNotThrow {
            insertRelationshipPair(users)
        }
    }

    @Test
    suspend fun `updated_at cannot precede created_at`() {
        assertFails {
            insertRelationshipPair(createdAt = now, updatedAt = before)
        }
    }

    @Test
    suspend fun `deactivated_at cannot precede created_at`() {
        assertFails {
            insertRelationshipPair(createdAt = now, deactivatedAt = before)
        }
    }

    @Test
    suspend fun `deleted_at cannot precede deactivated_at (when present)`() {
        assertFails {
            insertRelationshipPair(
                createdAt = before,
                deactivatedAt = after,
                deletedAt = now,
            )
        }
    }
}
