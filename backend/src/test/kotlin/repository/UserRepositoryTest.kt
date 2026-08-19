package repository

import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.repository.UserRepository
import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestFixtures.getProperty
import fixtures.TestFixtures.insertUser
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserRepositoryTest : RepositoryTest() {
    private val repository = UserRepository(database)

    @Test
    suspend fun `insert persists user`() {
        val user = User(name = "Test username")

        repository.insert(user)
        val stored = repository.find(user.id)

        assertNotNull(stored)
        assertEquals(user, stored)
    }

    @Test
    suspend fun `find returns null for unknown id`() {
        repeat(5) {
            insertUser()
        }

        val result = repository.find(UUID.randomUUID())

        assertNull(result)
    }

    @Test
    suspend fun `rename updates username`() {
        val id = insertUser(name = "Test username")

        repository.rename(id, "John")

        assertEquals("John", getProperty("users", "name", id))
    }

    @Test
    suspend fun `rename throws for unknown device`() {
        assertThrows<NoSuchElementException> {
            repository.rename(UUID.randomUUID(), "John")
        }
    }

    @Test
    suspend fun `deactivate marks active user as deactivated`() {
        val id = insertUser()

        val changed = repository.deactivate(id)

        assertTrue(changed)
        assertNotNull(
            getProperty("users", "deactivated_at", id) { instantOrNull(it) }
        )
    }

    @Test
    suspend fun `deactivate fails for already deactivated user`() {
        val id = insertUser(deactivatedAt = now())

        val changed = repository.deactivate(id)

        assertFalse(changed)
    }

    @Test
    suspend fun `deactivate fails for deleted user`() {
        val id = insertUser(deactivatedAt = now(), deletedAt = now())

        val changed = repository.deactivate(id)

        assertFalse(changed)
    }

    @Test
    suspend fun `deactivate cannot be performed twice`() {
        val id = insertUser()

        assertTrue(repository.deactivate(id))
        assertFalse(repository.deactivate(id))
    }

    @Test
    suspend fun `deactivate fails for unknown user`() {
        assertFalse(repository.deactivate(UUID.randomUUID()))
    }

    @Test
    suspend fun `reactivate clears deactivated state`() {
        val id = insertUser(deactivatedAt = now())

        val changed = repository.reactivate(id)

        assertTrue(changed)
        assertNull(
            getProperty("users", "deactivated_at", id) { instantOrNull(it) }
        )
    }

    @Test
    suspend fun `reactivate fails for active user`() {
        val id = insertUser()

        assertFalse(repository.reactivate(id))
    }

    @Test
    suspend fun `reactivate fails for deleted user`() {
        val id = insertUser(deactivatedAt = now(), deletedAt = now())

        assertFalse(repository.reactivate(id))
    }

    @Test
    suspend fun `reactivate cannot be performed twice`() {
        val id = insertUser(deactivatedAt = now())

        assertTrue(repository.reactivate(id))
        assertFalse(repository.reactivate(id))
    }

    @Test
    suspend fun `reactivate fails for unknown user`() {
        assertFalse(repository.reactivate(UUID.randomUUID()))
    }

    @Test
    suspend fun `user can be deactivated then reactivated`() {
        val id = insertUser()

        assertTrue(repository.deactivate(id))
        assertTrue(repository.reactivate(id))

        assertNull(
            getProperty("users", "deactivated_at", id) { instantOrNull(it) }
        )
    }

    @Test
    suspend fun `delete marks deactivated user as deleted`() {
        val id = insertUser(deactivatedAt = now())

        val changed = repository.delete(id)

        assertTrue(changed)
        assertNotNull(
            getProperty("users", "deleted_at", id) { instantOrNull(it) }
        )
    }

    @Test
    suspend fun `delete fails for active user`() {
        val id = insertUser()

        val changed = repository.delete(id)

        assertFalse(changed)
    }

    @Test
    suspend fun `delete cannot be performed twice`() {
        val id = insertUser(deactivatedAt = now())

        assertTrue(repository.delete(id))
        assertFalse(repository.delete(id))
    }

    @Test
    suspend fun `delete fails for unknown user`() {
        assertFalse(repository.delete(UUID.randomUUID()))
    }
}
