package repository

import dev.frammenti.fuckumeter.domain.Invite.InviteUser
import dev.frammenti.fuckumeter.extensions.withCode
import dev.frammenti.fuckumeter.repository.InviteRepository
import fixtures.TestCrypto.cipher
import fixtures.TestCrypto.code
import fixtures.TestCrypto.hasher
import fixtures.TestFixtures.insertInvite
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InviteRepositoryTest : RepositoryTest() {
    private val repository = InviteRepository(database, hasher, cipher)

    @Test
    suspend fun `insert persists invite`() {
        val invite = InviteUser(insertUser()).withCode(code())

        val id = repository.insert(invite)
        val stored = repository.find(id)

        assertNotNull(stored)
        assertEquals(invite, stored)
    }

    @Test
    suspend fun `find returns null for unknown id`() {
        repeat(5) { i ->
            insertInvite()
        }

        val result = repository.find(6)

        assertNull(result)
    }

    @Test
    suspend fun `findByCode returns invite with id`() {
        val code = "test"
        val id = insertInvite(code = code)

        val result = repository.findByCode(code)

        assertNotNull(result)
        assertEquals(id, result!!.id)
    }

    @Test
    suspend fun `findByCode returns null for unknown code`() {
        val code = "test"
        val id = insertInvite(code = code)

        val result = repository.findByCode("wrong")

        assertNull(result)
    }
}
