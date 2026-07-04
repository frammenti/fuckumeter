package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_RELATIONSHIP
import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_THRESHOLD
import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

class Relationship(
    val id: UUID,
    val userId: UUID,
    val partnerId: UUID,
    val otherRelationshipId: UUID,
    val nickname: String?,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_RELATIONSHIP,
    val notificationThreshold: Int = NOTIFICATION_THRESHOLD,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val deactivatedAt: OffsetDateTime?,
    val deletedAt: OffsetDateTime?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.uuid("user_id"),
        row.uuid("partner_id"),
        row.uuid("other_relationship_id"),
        row.stringOrNull("nickname"),
        row.boolean("notification_enabled"),
        row.int("notification_threshold"),
        row.offsetDateTime("created_at"),
        row.offsetDateTimeOrNull("updated_at"),
        row.offsetDateTimeOrNull("deactivated_at"),
        row.offsetDateTimeOrNull("deleted_at"),
    )
}
