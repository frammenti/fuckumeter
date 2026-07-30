package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.Encrypted
import dev.frammenti.fuckumeter.security.HmacHasher
import java.io.Serializable
import java.util.UUID
import kotliquery.Row

class InviteRepository(
    database: Database,
    private val hasher: HmacHasher,
    private val cipher: AesGcmCipher,
) : Repository(database) {
    private fun Invite.params(): Array<Pair<String, Serializable?>> {
        val encrypted = cipher.encrypt(code)
        val additionalProperties =
            when (this) {
                is Invite.InviteUser -> arrayOf("group_id" to groupId)
                is Invite.JoinGroup -> arrayOf("group_id" to groupId)
                is Invite.LinkDevice -> arrayOf("device_name" to deviceName)
                is Invite.Recovery ->
                    arrayOf("recovery_request_id" to recoveryRequestId)
            }

        return arrayOf(
                "created_by_user_id" to createdBy,
                "consumed_by_user_id" to consumedBy,
                "code_hash" to hasher.hash(code),
                "code_ciphertext" to encrypted.ciphertext,
                "code_nonce" to encrypted.nonce,
                "type" to type.name,
                "created_at" to createdAt,
                "expires_at" to expiresAt,
                "consumed_at" to consumedAt,
                "revoked_at" to revokedAt,
            )
            .plus(additionalProperties)
    }

    private fun mapWithCode(row: Row): Invite {
        val invite = Invite.factory(row)
        val encrypted = Encrypted(row)
        invite.initializeCode(cipher.decrypt(encrypted))
        return invite
    }

    fun findByCode(code: ByteArray): Invite? = session {
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

    fun findAllByUser(userId: UUID): List<Invite> = session {
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

    fun findLatestByUser(
        userId: UUID,
        type: Invite.InviteType,
    ): Invite? = session {
        single(
            sql(
                """
                    SELECT *
                    FROM invites
                    WHERE created_by_user_id = :user_id
                    AND type = :type::invite_type
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
    fun findLatestForGroup(groupId: UUID): Invite? = session {
        single(
            sql(
                """
                    SELECT *
                    FROM invites
                    WHERE group_id = :group_id
                    AND type = JOIN_GROUP
                    ORDER BY id DESC
                    LIMIT 1;
                    """,
                "group_id" to groupId,
            ),
            ::mapWithCode,
        )
    }

    fun insert(invite: Invite) = session {
        update(
                sql(
                    """
                    INSERT INTO invites (
                        created_by_user_id, code_hash,
                        code_ciphertext, code_nonce, type,
                        group_id, device_name, recovery_request_id,
                        created_at, expires_at
                    )
                    VALUES (
                        :created_by_user_id, :code_hash,
                        :code_ciphertext, :code_nonce, :type::invite_type,
                        :group_id, :device_name, :recovery_request_id,
                        :created_at, :expires_at
                    );
                    """,
                    *invite.params(),
                )
            )
            .expectOne()
    }
}
