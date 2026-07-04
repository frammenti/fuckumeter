package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_DEVICE
import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

class Device(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_DEVICE,
    val fcmToken: String?,
    val refreshToken: String,
    val createdAt: OffsetDateTime,
    val lastAccessedAt: OffsetDateTime?,
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
        row.offsetDateTime("created_at"),
        row.offsetDateTimeOrNull("last_accessed_at"),
    )
}
