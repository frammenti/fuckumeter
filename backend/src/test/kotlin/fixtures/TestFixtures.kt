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

    suspend fun insertRelationship(
        id: UUID,
        userId: UUID,
        partnerId: UUID,
        otherRelationshipId: UUID,
        nickname: String? = null,
        notificationEnabled: Boolean = false,
        notificationThreshold: Int = 80,
        createdAt: Instant = now(),
        updatedAt: Instant? = null,
        deactivatedAt: Instant? = null,
        deletedAt: Instant? = null,
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO relationships (
                        id, user_id, partner_id, other_relationship_id,
                        nickname, notification_enabled, notification_threshold,
                        created_at, updated_at, deactivated_at, deleted_at
                        )
                    VALUES (
                        :id, :user_id, :partner_id, :other_relationship_id,
                        :nickname, :notification_enabled, :notification_threshold,
                        :created_at, :updated_at, :deactivated_at, :deleted_at
                        );
                    """,
                    "id" to id,
                    "user_id" to userId,
                    "partner_id" to partnerId,
                    "other_relationship_id" to otherRelationshipId,
                    "nickname" to nickname,
                    "notification_enabled" to notificationEnabled,
                    "notification_threshold" to notificationThreshold,
                    "created_at" to createdAt,
                    "updated_at" to updatedAt,
                    "deactivated_at" to deactivatedAt,
                    "deleted_at" to deletedAt,
                )
            )
        }
        return id
    }

    suspend fun insertRelationshipPair(
        users: Pair<UUID, UUID> = runBlocking { insertUser() to insertUser() },
        nickname: String? = null,
        notificationEnabled: Boolean = false,
        notificationThreshold: Int = 80,
        createdAt: Instant = now(),
        updatedAt: Instant? = null,
        deactivatedAt: Instant? = null,
        deletedAt: Instant? = null,
    ): Pair<UUID, UUID> {
        val ids = UUID.randomUUID() to UUID.randomUUID()

        database.transaction {
            insertRelationship(
                id = ids.first,
                userId = users.first,
                partnerId = users.second,
                otherRelationshipId = ids.second,
                nickname = nickname,
                notificationEnabled = notificationEnabled,
                notificationThreshold = notificationThreshold,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deactivatedAt = deactivatedAt,
                deletedAt = deletedAt,
            )
            insertRelationship(
                id = ids.second,
                userId = users.second,
                partnerId = users.first,
                otherRelationshipId = ids.first,
                nickname = nickname,
                notificationEnabled = notificationEnabled,
                notificationThreshold = notificationThreshold,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deactivatedAt = deactivatedAt,
                deletedAt = deletedAt,
            )
        }
        return ids
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

    suspend fun insertMembership(
        userId: UUID = runBlocking { insertUser() },
        groupId: UUID = runBlocking { insertGroup() },
        shareRelationships: Boolean = false,
        joinedAt: Instant? = now(), // can be null
        leftAt: Instant? = null,
    ): Long {
        return database.session {
            insert(
                database.sql(
                    """
                    INSERT INTO memberships (
                        user_id, group_id, share_relationships,
                        joined_at, left_at
                        )
                    VALUES (
                        :user_id, :group_id, :share_relationships,
                        :joined_at, :left_at
                        );
                    """,
                    "user_id" to userId,
                    "group_id" to groupId,
                    "share_relationships" to shareRelationships,
                    "joined_at" to joinedAt,
                    "left_at" to leftAt,
                )
            )
        }!!
    }

    suspend fun insertDevice(
        id: UUID = UUID.randomUUID(),
        userId: UUID = runBlocking { insertUser() },
        name: String = "Test device",
        notificationEnabled: Boolean = false,
        fcmToken: String? = null,
        refreshToken: String = "token",
        createdAt: Instant = now(),
        lastSeenAt: Instant? = null,
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO devices (
                        id, user_id, name,
                        notification_enabled, fcm_token, refresh_token_hash,
                        created_at, last_seen_at
                    )
                    VALUES (
                        :id, :user_id, :name,
                        :notification_enabled, :fcm_token, :refresh_token_hash,
                        :created_at, :last_seen_at
                    );
                    """,
                    "id" to id,
                    "user_id" to userId,
                    "name" to name,
                    "notification_enabled" to notificationEnabled,
                    "fcm_token" to fcmToken,
                    "refresh_token_hash" to hasher.hash(refreshToken),
                    "created_at" to createdAt,
                    "last_seen_at" to lastSeenAt,
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

    suspend fun insertRecoveryRequest(
        userId: UUID = runBlocking { insertUser() },
        partnerId: UUID = runBlocking { insertUser() },
        inviteId: Long? = null,
        createdAt: Instant = now(),
        revokedAt: Instant? = null,
        revokedByPartnerAt: Instant? = null,
    ): Long {
        insertRelationshipPair(userId to partnerId)

        return database.session {
            insert(
                database.sql(
                    """
                    INSERT INTO recovery_requests (
                        user_id, partner_id, invite_id,
                        created_at, revoked_at, revoked_by_partner_at
                    )
                    VALUES (
                        :user_id, :partner_id, :invite_id,
                        :created_at, :revoked_at, :revoked_by_partner_at
                    );
                    """,
                    "user_id" to userId,
                    "partner_id" to partnerId,
                    "invite_id" to inviteId,
                    "created_at" to createdAt,
                    "revoked_at" to revokedAt,
                    "revoked_by_partner_at" to revokedByPartnerAt,
                )
            )
        }!!
    }
}
