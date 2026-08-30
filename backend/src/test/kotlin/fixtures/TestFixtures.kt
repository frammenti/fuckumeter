package fixtures

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.extensions.insert
import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestCrypto.cipher
import fixtures.TestCrypto.code
import fixtures.TestCrypto.hasher
import fixtures.TestDatabase.database
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotliquery.Row

object TestFixtures {
    private suspend fun <T> getProperty(
        table: String,
        column: String,
        id: Any,
        mapper: Row.(columnLabel: String) -> T,
    ): T? = database.session {
        single(
            database.sql(
                """
                SELECT $column
                FROM $table
                WHERE id = :id
                """,
                "id" to id,
            )
        ) { row ->
            row.mapper(column)
        }
    }

    suspend fun getString(
        table: String,
        column: String,
        id: Any,
    ): String? = getProperty(table, column, id) { stringOrNull(it) }

    suspend fun getBoolean(
        table: String,
        column: String,
        id: Any,
    ): Boolean? = getProperty(table, column, id) { boolean(it) }

    suspend fun getInstant(
        table: String,
        column: String,
        id: Any,
    ): Instant? = getProperty(table, column, id) { instantOrNull(it) }

    suspend fun getByteArray(
        table: String,
        column: String,
        id: Any,
    ): ByteArray? = getProperty(table, column, id) { bytesOrNull(it) }

    suspend fun insertUser(
        id: UUID = UUID.randomUUID(),
        name: String = "Test user",
        createdAt: Instant = now(),
        updatedAt: Instant? = null,
        deactivatedAt: Instant? = null,
        deletedAt: Instant? = null,
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO users (
                        id, name, created_at,
                        updated_at, deactivated_at, deleted_at
                        )
                    VALUES (
                        :id, :name, :created_at,
                        :updated_at, :deactivated_at, :deleted_at
                        );
                    """,
                    "id" to id,
                    "name" to name,
                    "created_at" to createdAt,
                    "updated_at" to updatedAt,
                    "deactivated_at" to deactivatedAt,
                    "deleted_at" to deletedAt,
                )
            )
        }
        return id
    }

    suspend fun insertDevice(
        id: UUID = UUID.randomUUID(),
        userId: UUID = runBlocking { insertUser() },
        name: String = "Test device",
        notificationEnabled: Boolean = false,
        createdAt: Instant = now(),
        refreshToken: String = "token",
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO devices (
                        id, user_id, name, notification_enabled,
                        refresh_token_hash, created_at
                    )
                    VALUES (
                        :id, :user_id, :name, :notification_enabled,
                        :refresh_token_hash, :created_at
                    );
                    """,
                    "id" to id,
                    "user_id" to userId,
                    "name" to name,
                    "notification_enabled" to notificationEnabled,
                    "refresh_token_hash" to hasher.hash(refreshToken),
                    "created_at" to createdAt,
                )
            )
        }
        return id
    }

    suspend fun insertGroup(
        id: UUID = UUID.randomUUID(),
        name: String = "Test group",
        updatedBy: UUID? = null,
        createdAt: Instant = now(),
        updatedAt: Instant? = null,
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO groups (id, name, updated_by_user_id, created_at, updated_at)
                    VALUES (:id, :name, :updated_by_user_id, :created_at, :updated_at);
                    """,
                    "id" to id,
                    "name" to name,
                    "updated_by_user_id" to updatedBy,
                    "created_at" to createdAt,
                    "updated_at" to updatedAt,
                )
            )
        }
        return id
    }

    suspend fun insertInvite(
        createdBy: UUID = runBlocking { insertUser() },
        consumedBy: UUID? = null,
        code: String = code(),
        type: Invite.Type = Invite.Type.INVITE_USER,
        partnerId: UUID? = null,
        groupId: UUID? = null,
        createdAt: Instant = now(),
        expiresAt: Instant = createdAt.plus(1, ChronoUnit.DAYS),
        consumedAt: Instant? = null,
        revokedAt: Instant? = null,
    ): Long {
        val encrypted = cipher.encrypt(code)

        return database.session {
            insert(
                database.sql(
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
                    "created_by_user_id" to createdBy,
                    "consumed_by_user_id" to consumedBy,
                    "code_hash" to hasher.hash(code),
                    "code_ciphertext" to encrypted.ciphertext,
                    "code_nonce" to encrypted.nonce,
                    "type" to type.name,
                    "partner_id" to partnerId,
                    "group_id" to groupId,
                    "created_at" to createdAt,
                    "expires_at" to expiresAt,
                    "consumed_at" to consumedAt,
                    "revoked_at" to revokedAt,
                )
            )
        }!!
    }
}
