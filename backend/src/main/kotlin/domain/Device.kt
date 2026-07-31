package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_DEVICE
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID
import kotliquery.Row

data class Device(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val name: String,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_DEVICE,
    val fcmToken: String? = null,
    val createdAt: Instant = now(),
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
        row.instant("created_at"),
        row.instantOrNull("last_seen_at"),
    )
}
