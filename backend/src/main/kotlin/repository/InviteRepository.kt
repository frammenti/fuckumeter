package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.InternalId.WithId
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.extensions.expectNotNull
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.extensions.insert
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
    private fun <I : Invite> WithCode<I>.toParams():
        Array<Pair<String, Serializable?>> {
        val (ciphertext, nonce) = cipher.encrypt(code)

        return arrayOf(
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
        ) +
            when (invite) {
                is InviteUser,
                is LinkDevice ->
                    arrayOf("partner_id" to null, "group_id" to null)
                is JoinGroup ->
                    arrayOf("partner_id" to null, "group_id" to invite.groupId)
                is Recovery ->
                    arrayOf(
                        "partner_id" to invite.partnerId,
                        "group_id" to null,
                    )
            }
    }

    private fun <I : Invite> Row.toInvite(): I {
        val type = Type.valueOf(string("type"))
        val lifecycle =
            Lifecycle(
                uuid("created_by_user_id"),
                uuidOrNull("consumed_by_user_id"),
                instant("created_at"),
                instant("expires_at"),
                instantOrNull("consumed_at"),
                instantOrNull("revoked_at"),
            )

        @Suppress("UNCHECKED_CAST")
        return when (type) {
            Type.INVITE_USER -> InviteUser(lifecycle)
            Type.JOIN_GROUP ->
                JoinGroup(
                    uuid("group_id"),
                    lifecycle,
                )
            Type.LINK_DEVICE -> LinkDevice(lifecycle)
            Type.RECOVERY ->
                Recovery(
                    uuid("partner_id"),
                    lifecycle,
                )
        }
            as I
    }

    private fun <I : Invite> Row.toInviteWithCode(): WithCode<I> {
        val invite = toInvite<I>()
        val encrypted =
            Encrypted(
                bytes("code_ciphertext"),
                bytes("code_nonce"),
            )

        return WithCode(invite, cipher.decrypt(encrypted))
    }

    private fun <I : Invite> Row.toInviteWithId(): WithId<I> {
        val invite = toInvite<I>()
        return WithId(invite, long("id"))
    }

    suspend fun find(id: Long): WithCode<Invite>? = session {
        single(
            sql(
                """
                SELECT *
                FROM invites
                WHERE id = :id;
                """,
                "id" to id,
            )
        ) { row ->
            row.toInviteWithCode()
        }
    }

    suspend fun findByCode(code: String): WithId<Invite>? = session {
        single(
            sql(
                """
                SELECT *
                FROM active_invites
                WHERE code_hash = :code_hash;
                """,
                "code_hash" to hasher.hash(code),
            )
        ) { row ->
            row.toInviteWithId()
        }
    }

    suspend fun <I : Invite> findLatestByUser(
        userId: UUID,
        type: TypeOf<I>,
        groupId: UUID? = null,
    ): WithCode<I>? = session {
        single(
            sql(
                """
                    SELECT *
                    FROM invites
                    WHERE created_by_user_id = :user_id
                      AND type = :type::invite_type
                      AND group_id IS NOT DISTINCT FROM :group_id;
                    ORDER BY id DESC
                    LIMIT 1;
                    """,
                "user_id" to userId,
                "type" to type.name,
                "group_id" to groupId,
            )
        ) { row ->
            row.toInviteWithCode()
        }
    }

    suspend fun findAllByUser(userId: UUID): List<Invite> = session {
        list(
            sql(
                """
                SELECT DISTINCT ON (type, group_id) *
                FROM invites
                WHERE created_by_user_id = :user_id
                ORDER BY type, group_id, id DESC;
                """,
                "user_id" to userId,
            )
        ) { row ->
            row.toInvite()
        }
    }

    suspend fun insert(invite: WithCode<Invite>): Long = session {
        insert(
                sql(
                    """
                    INSERT INTO invites (
                        created_by_user_id, consumed_by_user_id,
                        code_hash, code_ciphertext, code_nonce,
                        type, partner_id, group_id,
                        created_at, expires_at, consumed_at, revoked_at
                    )
                    VALUES (
                        :created_by_user_id, :consumed_by_user_id,
                        :code_hash, :code_ciphertext, :code_nonce,
                        :type::invite_type, :partner_id, :group_id,
                        :created_at, :expires_at, :consumed_at, :revoked_at
                    );
                    """,
                    *invite.toParams(),
                )
            )
            .expectNotNull()
    }

    suspend fun consume(id: Long, userId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE active_invites
                    SET consumed_by_user_id = :user_id,
                        consumed_at = now()
                    WHERE id = :id;
                    """,
                    "id" to id,
                    "user_id" to userId,
                )
            )
            .expectOne()
    }

    suspend fun revoke(
        userId: UUID,
        type: TypeOf<Invite>,
        groupId: UUID? = null,
    ) = session {
        update(
                sql(
                    """
                    UPDATE active_invites
                    SET revoked_at = now()
                    WHERE created_by_user_id = :user_id
                      AND type = :type::invite_type
                      AND group_id IS NOT DISTINCT FROM :group_id;
                    """,
                    "user_id" to userId,
                    "type" to type.name,
                    "group_id" to groupId,
                )
            )
            .expectOne()
    }
}
