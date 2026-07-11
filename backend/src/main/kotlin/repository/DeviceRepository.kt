package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database.session
import dev.frammenti.fuckumeter.db.sql
import dev.frammenti.fuckumeter.domain.Device
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.security.RefreshHasher
import java.time.Instant
import java.util.UUID

class DeviceRepository {
    private fun Device.params() =
        arrayOf(
            "id" to id,
            "user_id" to userId,
            "name" to name,
            "notification_enabled" to notificationEnabled,
            "fcm_token" to fcmToken,
            "refresh_token_hash" to RefreshHasher.hash(refreshToken),
            "created_at" to createdAt,
            "last_seen_at" to lastSeenAt,
        )

    fun find(id: UUID): Device? = session {
        single(
            sql(
                """
                SELECT *
                FROM devices
                WHERE id = :id;
                """,
                "id" to id,
            ),
            ::Device,
        )
    }

    fun findAllForUser(userId: UUID): List<Device> = session {
        list(
            sql(
                """
                SELECT *
                FROM devices
                WHERE user_id = :user_id;
                """,
                "user_id" to userId,
            ),
            ::Device,
        )
    }

    fun insert(device: Device) = session {
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
                    *device.params(),
                )
            )
            .expectOne()
    }

    fun rename(id: UUID, name: String) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET name = :name,
                        updated_at = now()
                    WHERE id = :id
                    """,
                    "id" to id,
                    "name" to name,
                )
            )
            .expectOne()
    }

    fun enableNotification(id: UUID, enable: Boolean) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET notification_enabled = :notification_enabled,
                        updated_at = now()
                    WHERE id = :id
                    """,
                    "id" to id,
                    "notification_enabled" to enable,
                )
            )
            .expectOne()
    }

    fun updateRefreshToken(id: UUID, token: String) = session {
        update(
                sql(
                    """
                    UPDATE devices
                    SET refresh_token_hash = :refresh_token_hash
                    WHERE id = :id
                    """,
                    "id" to id,
                    "refresh_token_hash" to RefreshHasher.hash(token),
                )
            )
            .expectOne()
    }

    fun updateFcmToken(id: UUID, token: String) = session {
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

    fun updateLastSeen(id: UUID, time: Instant) = session {
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
}
