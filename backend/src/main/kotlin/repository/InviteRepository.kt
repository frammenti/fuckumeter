package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.Encrypted
import dev.frammenti.fuckumeter.security.HmacHasher
import java.util.UUID
import kotliquery.Row

class InviteRepository(
    database: Database,
    private val hasher: HmacHasher,
    private val cipher: AesGcmCipher,
) : Repository(database) {
    private fun InviteWithCode.toParams(): Array<Pair<String, Any?>> {
        val additionalProperties =
            when (this.invite) {
                is InviteUser,
                is LinkDevice -> emptyArray()
                is JoinGroup -> arrayOf("group_id" to invite.groupId)
                is Recovery ->
                    arrayOf("relationship_id" to invite.relationshipId)
            }

        val (ciphertext, nonce) = cipher.encrypt(code)

        return arrayOf<Pair<String, Any?>>(
                "created_by_user_id" to invite.createdBy,
                "consumed_by_user_id" to invite.consumedBy,
                "code_hash" to hasher.hash(code),
                "code_ciphertext" to ciphertext,
                "code_nonce" to nonce,
                "type" to invite.type.name,
                "created_at" to invite.createdAt,
                "expires_at" to invite.expiresAt,
                "consumed_at" to invite.consumedAt,
                "revoked_at" to invite.revokedAt,
            )
            .plus(additionalProperties)
    }

    private fun Row.toInvite(): Invite {
        val type = InviteType.valueOf(string("type"))
        val lifecycle =
            Lifecycle(
                uuid("created_by_user_id"),
                uuidOrNull("consumed_by_user_id"),
                instant("created_at"),
                instant("expires_at"),
                instantOrNull("consumed_at"),
                instantOrNull("revoked_at"),
            )

        return when (type) {
            InviteType.INVITE_USER -> InviteUser(lifecycle)
            InviteType.JOIN_GROUP ->
                JoinGroup(
                    uuid("group_id"),
                    lifecycle,
                )
            InviteType.LINK_DEVICE -> LinkDevice(lifecycle)
            InviteType.RECOVERY ->
                Recovery(
                    uuid("relationship_id"),
                    lifecycle,
                )
        }
    }

    private fun Row.toInviteWithCode(): InviteWithCode {
        val invite = toInvite()
        val encrypted =
            Encrypted(
                bytes("code_ciphertext"),
                bytes("code_nonce"),
            )

        return InviteWithCode(invite, cipher.decrypt(encrypted))
    }

    private fun Row.toInviteWithId(): InviteWithId {
        val invite = toInvite()
        return InviteWithId(invite, int("id"))
    }

    suspend fun findByCode(code: String): InviteWithId? = session {
        single(
            sql(
                """
                SELECT *
                FROM active_invites
                WHERE code_hash = :code_hash
                """,
                "code_hash" to hasher.hash(code),
            )
        ) { row ->
            row.toInviteWithId()
        }
    }

    suspend fun findAllByUser(userId: UUID): List<Invite> = session {
        list(
            sql(
                """
                SELECT *
                FROM invites
                WHERE created_by_user_id = :user_id
                """,
                "user_id" to userId,
            )
        ) { row ->
            row.toInvite()
        }
    }

    suspend fun findLatestByUser(
        userId: UUID,
        type: InviteType,
    ): InviteWithCode? = session {
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
            )
        ) { row ->
            row.toInviteWithCode()
        }
    }

    // Group invite is reused even if created by another user
    suspend fun findLatestForGroup(groupId: UUID): InviteWithCode? = session {
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
            )
        ) { row ->
            row.toInviteWithCode()
        }
    }

    suspend fun insert(invite: InviteWithCode) = session {
        update(
                sql(
                    """
                    INSERT INTO invites (
                        created_by_user_id, consumed_by_user_id,
                        code_hash, code_ciphertext, code_nonce,
                        type, group_id, recovery_request_id,
                        created_at, expires_at, consumed_at, revoked_at
                    )
                    VALUES (
                        :created_by_user_id, :consumed_by_user_id,
                        :code_hash, :code_ciphertext, :code_nonce,
                        :type::invite_type, :group_id, :recovery_request_id,
                        :created_at, :expires_at, :consumed_at, :revoked_at
                    );
                    """,
                    *invite.toParams(),
                )
            )
            .expectOne()
    }

    suspend fun consume(id: Int, userId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE invites
                    SET consumed_by_user_id = :user_id,
                        consumed_at = now()
                    WHERE id = :id
                      AND (consumed_at IS NULL OR type = 'JOIN_GROUP'::invite_type)
                      AND revoked_at IS NULL
                      AND expires_at > now();
                    """,
                    "id" to id,
                    "user_id" to userId,
                )
            )
            .expectOne()
    }

    suspend fun revoke(userId: UUID, type: InviteType, groupId: UUID? = null) = session {
        update(
                sql(
                    """
                    UPDATE active_invites
                    SET revoked_at = now()
                    WHERE created_by_user_id = :user_id
                      AND type = :type::invite_type
                      AND group_id = :group_id;
                    """,
                    "user_id" to userId,
                    "type" to type.name,
                    "group_id" to groupId,
                )
            )
            .expectOne()
    }
}
