package schema

import dev.frammenti.fuckumeter.domain.Invite.Type
import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestFixtures.getInstant
import fixtures.TestFixtures.insertGroup
import fixtures.TestFixtures.insertInvite
import fixtures.TestFixtures.insertUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InviteSchemaTest : SchemaTest() {
    @Test
    suspend fun `code must be unique`() {
        val code = "test"

        insertInvite(code = code)
        assertFails { insertInvite(code = code) }
    }

    @Test
    suspend fun `new invite revokes previous of same type`() {
        val user = insertUser()
        val invite = insertInvite(createdBy = user, type = Type.INVITE_USER)

        insertInvite(createdBy = user, type = Type.INVITE_USER)
        val revoked = getInstant("invites", "revoked_at", invite)

        assertNotNull(revoked)
        assertTrue(revoked!! < now())
    }

    @Test
    suspend fun `new invite does not affect previously revoked of same type`() {
        val user = insertUser()
        val invite =
            insertInvite(
                createdBy = user,
                type = Type.INVITE_USER,
                createdAt = before,
                revokedAt = now,
            )

        insertInvite(createdBy = user, type = Type.INVITE_USER)
        val revoked = getInstant("invites", "revoked_at", invite)

        assertNotNull(revoked)
        assertEquals(now, revoked!!)
    }

    @Test
    suspend fun `new invite does not affect previous of different type`() {
        val user = insertUser()
        val invite = insertInvite(createdBy = user, type = Type.INVITE_USER)

        insertInvite(createdBy = user, type = Type.LINK_DEVICE)
        val revoked = getInstant("invites", "revoked_at", invite)

        assertNull(revoked)
    }

    @Test
    suspend fun `join group requires group_id`() {
        assertFails {
            insertInvite(type = Type.JOIN_GROUP, groupId = null)
        }
    }

    @Test
    suspend fun `only join group can have group_id`() {
        val group = insertGroup()

        assertFails {
            insertInvite(type = Type.INVITE_USER, groupId = group)
        }

        assertFails {
            insertInvite(type = Type.LINK_DEVICE, groupId = group)
        }

        assertFails {
            insertInvite(
                type = Type.RECOVERY,
                partnerId = insertUser(),
                groupId = group,
            )
        }
    }

    @Test
    suspend fun `recovery requires partner_id`() {
        assertFails {
            insertInvite(type = Type.RECOVERY, partnerId = null)
        }
    }

    @Test
    suspend fun `only recovery can have partner_id`() {
        val partner = insertUser()

        assertFails {
            insertInvite(type = Type.INVITE_USER, partnerId = partner)
        }

        assertFails {
            insertInvite(type = Type.LINK_DEVICE, partnerId = partner)
        }

        assertFails {
            insertInvite(
                type = Type.JOIN_GROUP,
                partnerId = partner,
                groupId = insertGroup(),
            )
        }
    }

    @Test
    suspend fun `consume requires by user and time together`() {
        assertFails {
            insertInvite(
                consumedBy = insertUser(),
                consumedAt = null,
            )
        }

        assertFails {
            insertInvite(consumedBy = null, consumedAt = now)
        }
    }

    @Test
    suspend fun `expires_at cannot precede created_at`() {
        assertFails {
            insertInvite(createdAt = now, expiresAt = before)
        }
    }

    @Test
    suspend fun `consumed_at cannot precede created_at`() {
        assertFails {
            insertInvite(
                consumedBy = insertUser(),
                createdAt = now,
                consumedAt = before,
                expiresAt = after,
            )
        }
    }

    @Test
    suspend fun `consumed_at must precede expires_at`() {
        assertFails {
            insertInvite(
                consumedBy = insertUser(),
                createdAt = before,
                consumedAt = after,
                expiresAt = now,
            )
        }
    }

    @Test
    suspend fun `revoked_at cannot precede created_at`() {
        assertFails {
            insertInvite(createdAt = now, revokedAt = before)
        }
    }
}
