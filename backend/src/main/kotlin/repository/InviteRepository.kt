package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.db.sql
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.security.AesGcmCipher.Encrypted
import dev.frammenti.fuckumeter.security.InviteCipher
import dev.frammenti.fuckumeter.security.InviteHasher
import kotliquery.Row
import java.util.UUID

class InviteRepository {
    fun getInvitesByUser(userId: UUID): List<Invite> = Database.session {
        list(
            sql(
                """
                SELECT *
                FROM invites
                WHERE created_by_user_id = :user_id
                """,
                "user_id" to userId,
            ),
            Invite::factory,
        )
    }

    fun findByCode(code: String): Invite? = Database.session {
        single(
            sql(
                """
                SELECT *
                FROM invites
                WHERE code_hash = :code
                """,
                "code" to code,
            ),
            Invite::factory,
        )
    }

    private fun mapWithCode(row: Row): Invite {
        val invite = Invite.factory(row)
        val encrypted = Encrypted(row)
        invite.initializeCode(InviteCipher.decrypt(encrypted))
        return invite
    }

    fun findLatestByCreatorAndType(
        userId: UUID,
        type: Invite.InviteType,
    ): Invite? = Database.session {
        single(
            sql(
                """
                    SELECT *
                    FROM invites
                    WHERE created_by_user_id = :user_id
                    AND type = :type
                    ORDER BY id DESC
                    LIMIT 1;
                    """,
                "user_id" to userId,
                "type" to type.name,
            ),
            ::mapWithCode,
        )
    }

    // Group invite is reused even if created by another user
    fun findLatestForGroup(groupId: UUID): Invite? = Database.session {
        single(
            sql(
                """
                    SELECT *
                    FROM invites
                    WHERE group_id = :group_id
                    AND type = :type
                    ORDER BY id DESC
                    LIMIT 1;
                    """,
                "group_id" to groupId,
                "type" to Invite.InviteType.JOIN_GROUP.name,
            ),
            ::mapWithCode,
        )
    }
}
