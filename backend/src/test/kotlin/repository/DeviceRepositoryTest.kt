package repository

import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestCrypto.hasher
import fixtures.TestFixtures.getBoolean
import fixtures.TestFixtures.getByteArray
import fixtures.TestFixtures.getInstant
import fixtures.TestFixtures.getString
import fixtures.TestFixtures.insertDevice
import fixtures.TestFixtures.insertUser
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeviceRepositoryTest : RepositoryTest() {
    private val repository = DeviceRepository(database, hasher)
    private val now = now()

    @Test
    suspend fun `insert persists device`() {
        val device =
            Device(
                name = "Test device",
                userId = insertUser(),
            )

        repository.insert(device, "token")
        val stored = repository.find(device.id)

        assertNotNull(stored)
        assertEquals(device, stored)
    }

    @Test
    suspend fun `find returns null for unknown id`() {
        repeat(5) {
            insertDevice()
        }

        val result = repository.find(UUID.randomUUID())

        assertNull(result)
    }

    @Test
    suspend fun `findAllForUser returns only user devices`() {
        val userA = insertUser()
        val userB = insertUser()
        repeat(2) {
            insertDevice(userId = userA)
        }
        insertDevice(userId = userB)

        val devices = repository.findAllForUser(userA)

        assertEquals(2, devices.size)
        assertTrue(devices.all { it.userId == userA })
    }

    @Test
    suspend fun `findAllForUser returns empty list when user has no devices`() {
        val userA = insertUser()
        val userB = insertUser()
        repeat(2) {
            insertDevice(userId = userB)
        }

        val devices = repository.findAllForUser(userA)

        assertTrue(devices.isEmpty())
    }

    @Test
    suspend fun `belongsToUser is true for device owned by the user`() {
        val userId = insertUser()
        val deviceId = insertDevice(userId = userId)

        assertTrue(repository.belongsToUser(deviceId, userId))
    }

    @Test
    suspend fun `belongsToUser is false for unknown user`() {
        val deviceId = insertDevice()

        assertFalse(repository.belongsToUser(deviceId, UUID.randomUUID()))
    }

    @Test
    suspend fun `belongsToUser is false for unknown device`() {
        val userId = insertUser()

        assertFalse(repository.belongsToUser(UUID.randomUUID(), userId))
    }

    @Test
    suspend fun `rename updates device name`() {
        val id = insertDevice(name = "Test device")

        repository.rename(id, "Phone")

        assertEquals("Phone", getString("devices", "name", id))
    }

    @Test
    suspend fun `rename throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.rename(UUID.randomUUID(), "Phone")
        }
    }

    @Test
    suspend fun `enableNotification changes notification flag`() {
        val id = insertDevice(notificationEnabled = false)

        repository.enableNotification(id, true)

        assertTrue(getBoolean("devices", "notification_enabled", id)!!)
    }

    @Test
    suspend fun `enableNotification throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.enableNotification(UUID.randomUUID(), true)
        }
    }

    @Test
    suspend fun `updateRefreshToken replaces refresh token`() {
        val userId = insertUser()
        val id = insertDevice(userId = userId, refreshToken = "old-token")

        val changed =
            repository.updateRefreshToken(
                id,
                "old-token",
                "new-token",
            )

        assertEquals(userId, changed)
        assertArrayEquals(
            hasher.hash("new-token"),
            getByteArray("devices", "refresh_token_hash", id),
        )
    }

    @Test
    suspend fun `updateRefreshToken fails when old token does not match`() {
        val id = insertDevice(refreshToken = "old-token")

        val missing =
            repository.updateRefreshToken(
                id,
                "wrong-token",
                "new-token",
            )

        assertNull(missing)
    }

    @Test
    suspend fun `updateFcmToken changes token`() {
        val id = insertDevice()

        repository.updateFcmToken(id, "new-token")

        assertEquals(
            "new-token",
            getString("devices", "fcm_token", id),
        )
    }

    @Test
    suspend fun `updateFcmToken throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.updateFcmToken(UUID.randomUUID(), "token")
        }
    }

    @Test
    suspend fun `updateLastSeen stores timestamp`() {
        val id = insertDevice(createdAt = now)

        repository.updateLastSeen(id, now)

        assertEquals(
            now,
            getInstant("devices", "last_seen_at", id),
        )
    }

    @Test
    suspend fun `updateLastSeen throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.updateLastSeen(UUID.randomUUID(), now())
        }
    }

    @Test
    suspend fun `delete removes device`() {
        val id = insertDevice()

        repository.delete(id)

        assertNull(repository.find(id))
    }

    @Test
    suspend fun `delete throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.delete(UUID.randomUUID())
        }
    }
}
