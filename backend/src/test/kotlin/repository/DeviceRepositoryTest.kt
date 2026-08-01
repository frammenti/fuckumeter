package repository

import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestCrypto.hasher
import fixtures.TestFixtures.getProperty
import fixtures.TestFixtures.insertDevice
import fixtures.TestFixtures.insertUser
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeviceRepositoryTest : RepositoryTest() {
    private val repository = DeviceRepository(database, hasher)

    @Test
    fun `insert persists device`() {
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
    fun `find returns null for unknown id`() {
        repeat(5) {
            insertDevice()
        }

        val result = repository.find(UUID.randomUUID())

        assertNull(result)
    }

    @Test
    fun `findAllForUser returns only user devices`() {
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
    fun `findAllForUser returns empty list when user has no devices`() {
        val userA = insertUser()
        val userB = insertUser()
        repeat(2) {
            insertDevice(userId = userB)
        }

        val devices = repository.findAllForUser(userA)

        assertTrue(devices.isEmpty())
    }

    @Test
    fun `rename updates device name`() {
        val id = insertDevice(name = "Test device")

        repository.rename(id, "Phone")

        assertEquals("Phone", getProperty("devices", "name", id))
    }

    @Test
    fun `rename throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.rename(UUID.randomUUID(), "Phone")
        }
    }

    @Test
    fun `enableNotification changes notification flag`() {
        val id = insertDevice(notificationEnabled = false)

        repository.enableNotification(id, true)

        assertTrue(
            getProperty("devices", "notification_enabled", id) { boolean(it) }!!
        )
    }

    @Test
    fun `enableNotification throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.enableNotification(UUID.randomUUID(), true)
        }
    }

    @Test
    fun `updateRefreshToken replaces refresh token`() {
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
            getProperty("devices", "refresh_token_hash", id) { bytes(it) },
        )

        val repeated =
            repository.updateRefreshToken(
                id,
                "old-token",
                "new-token",
            )

        assertNull(repeated)

        val changed2 =
            repository.updateRefreshToken(
                id,
                "new-token",
                "newer-token",
            )

        assertEquals(userId, changed2)
        assertArrayEquals(
            hasher.hash("newer-token"),
            getProperty("devices", "refresh_token_hash", id) { bytes(it) },
        )
    }

    @Test
    fun `updateRefreshToken fails when old token does not match`() {
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
    fun `updateFcmToken changes token`() {
        val id = insertDevice()

        repository.updateFcmToken(id, "new-token")

        assertEquals(
            "new-token",
            getProperty("devices", "fcm_token", id),
        )
    }

    @Test
    fun `updateFcmToken throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.updateFcmToken(UUID.randomUUID(), "token")
        }
    }

    @Test
    fun `updateLastSeen stores timestamp`() {

        val id = insertDevice()
        val now = now()

        repository.updateLastSeen(id, now)

        assertEquals(
            now,
            getProperty("devices", "last_seen_at", id) { instantOrNull(it) },
        )
    }

    @Test
    fun `updateLastSeen throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.updateLastSeen(UUID.randomUUID(), now())
        }
    }

    @Test
    fun `delete removes device`() {
        val id = insertDevice()

        repository.delete(id)

        assertNull(repository.find(id))
    }

    @Test
    fun `delete throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.delete(UUID.randomUUID())
        }
    }
}
