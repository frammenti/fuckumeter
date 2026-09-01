package schema

import fixtures.TestFixtures.insertRecoveryRequest
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class RecoveryRequestSchemaTest : SchemaTest() {
    @Test
    suspend fun `one open request per user`() {
        val user = insertUser()

        insertRecoveryRequest(userId = user)

        assertFails {
            insertRecoveryRequest(userId = user)
        }
    }

    @Test
    suspend fun `can create new request if previous is revoked`() {
        val user = insertUser()

        insertRecoveryRequest(userId = user, createdAt = now, revokedAt = now)

        assertDoesNotThrow {
            insertRecoveryRequest(userId = user)
        }
    }

    @Test
    suspend fun `cannot create new request if previous is revoked by partner`() {
        val user = insertUser()

        insertRecoveryRequest(
            userId = user,
            createdAt = now,
            revokedByPartnerAt = now,
        )

        assertFails {
            insertRecoveryRequest(userId = user)
        }
    }

    @Test
    suspend fun `one open request per partner`() {
        val partner = insertUser()

        insertRecoveryRequest(partnerId = partner)

        assertFails {
            insertRecoveryRequest(partnerId = partner)
        }
    }

    @Test
    suspend fun `can be target of new request if previous is revoked`() {
        val partner = insertUser()

        insertRecoveryRequest(
            partnerId = partner,
            createdAt = now,
            revokedAt = now,
        )

        assertDoesNotThrow {
            insertRecoveryRequest(partnerId = partner)
        }
    }

    @Test
    suspend fun `can be target of new request if previous is revoked by partner`() {
        val partner = insertUser()

        insertRecoveryRequest(
            partnerId = partner,
            createdAt = now,
            revokedByPartnerAt = now,
        )

        assertDoesNotThrow {
            insertRecoveryRequest(partnerId = partner)
        }
    }

    @Test
    suspend fun `revoked_at cannot precede created_at`() {
        assertFails {
            insertRecoveryRequest(createdAt = now, revokedAt = before)
        }

        assertFails {
            insertRecoveryRequest(createdAt = now, revokedByPartnerAt = before)
        }
    }
}
