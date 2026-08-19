package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.security.HmacHasher
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID
import kotliquery.Row

class DeviceRepository(database: Database, private val hasher: HmacHasher) :
    Repository(database) {
    private fun Device.toParams() =
        arrayOf(
            "id" to id,
            "user_id" to userId,
            "name" to name,
            "notification_enabled" to notificationEnabled,
            "fcm_token" to fcmToken,
            "created_at" to createdAt,
            "last_seen_at" to lastSeenAt,
        )

    private fun Row.toDevice() =
        Device(
            id = uuid("id"),
            userId = uuid("user_id"),
            name = string("name"),
            notificationEnabled = boolean("notification_enabled"),
            fcmToken = stringOrNull("fcm_token"),
            createdAt = instant("created_at"),
            lastSeenAt = instantOrNull("last_seen_at"),
        )

    suspend fun find(id: UUID): Device? = session {
        single(
            sql(
                """
                SELECT *
                FROM devices
                WHERE id = :id;
                """,
                "id" to id,
            )
        ) { row ->
            row.toDevice()
        }
    }

    suspend fun findAllForUser(userId: UUID): List<Device> = session {
        list(
            sql(
                """
                SELECT *
                FROM devices
                WHERE user_id = :user_id;
                """,
                "user_id" to userId,
            )
        ) { row ->
            row.toDevice()
        }
    }

    suspend fun belongsToUser(deviceId: UUID, userId: UUID): Boolean = session {
        single(
            sql(
                """
                    SELECT 1
                    FROM devices
                    WHERE id = :id
                      AND user_id = :user_id;
                    """,
                "id" to deviceId,
                "user_id" to userId,
            )
        ) { row ->
            row.int(1)
        } == 1
    }

    suspend fun insert(device: Device, refreshToken: String) = session {
        update(
                sql(
                    """
                    INSERT INTO devices (
                        id, user_id, name, notification_enabled,
                        fcm_token, refresh_token_hash, created_at
                    )
                    VALUES (
                        :id, :user_id, :name, :notification_enabled,
                        :fcm_token, :refresh_token_hash, :created_at
                    );
                    """,
                    "refresh_token_hash" to hasher.hash(refreshToken),
                    *device.toParams(),
                )
            )
            .expectOne()
    }

    suspend fun rename(id: UUID, name: String) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET name = :name
                    WHERE id = :id
                    """,
                    "id" to id,
                    "name" to name,
                )
            )
            .expectOne()
    }

    suspend fun enableNotification(id: UUID, enable: Boolean) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET notification_enabled = :notification_enabled
                    WHERE id = :id
                    """,
                    "id" to id,
                    "notification_enabled" to enable,
                )
            )
            .expectOne()
    }

    suspend fun updateRefreshToken(
        id: UUID,
        oldToken: String,
        newToken: String,
    ) = session {
        single(
            sql(
                """
                    UPDATE devices
                    SET refresh_token_hash = :newHash
                    WHERE id = :id
                      AND refresh_token_hash = :oldHash
                    RETURNING user_id;
                    """,
                "id" to id,
                "oldHash" to hasher.hash(oldToken),
                "newHash" to hasher.hash(newToken),
            )
        ) { row ->
            row.uuid("user_id")
        }
    }

    suspend fun updateFcmToken(id: UUID, token: String) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET fcm_token = :fcm_token
                    WHERE id = :id
                    """,
                    "id" to id,
                    "fcm_token" to token,
                )
            )
            .expectOne()
    }

    suspend fun updateLastSeen(id: UUID, time: Instant = now()) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET last_seen_at = :last_seen_at
                    WHERE id = :id
                    """,
                    "id" to id,
                    "last_seen_at" to time,
                )
            )
            .expectOne()
    }

    suspend fun delete(id: UUID) = session {
        update(sql("DELETE FROM devices WHERE id = :id", "id" to id))
            .expectOne()
    }
}
