package schema

import fixtures.TestFixtures.insertDevice
import fixtures.TestFixtures.insertInvite
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Test

class DeviceSchemaTest : SchemaTest() {
    @Test
    suspend fun `fcm token must be unique`() {
        val token = "test"

        insertDevice(fcmToken = token)
        assertFails { insertDevice(fcmToken = token) }
    }

    @Test
    suspend fun `last_seen_at cannot precede created_at`() {
        assertFails {
            insertDevice(createdAt = now, lastSeenAt = before)
        }
    }
}
