package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_DEVICE
import kotliquery.Row
import java.time.Instant
import java.util.UUID

class Device(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val name: String,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_DEVICE,
    val fcmToken: String? = null,
    val refreshToken: String,
    val createdAt: Instant = Instant.now(),
    val lastSeenAt: Instant? = null,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.uuid("user_id"),
        row.string("name"),
        row.boolean("notification_enabled"),
        row.stringOrNull("fcm_token"),
        row.string("refresh_token_hash"),
        row.instant("created_at"),
        row.instantOrNull("last_seen_at"),
    )
}
