package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_RELATIONSHIP
import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_THRESHOLD
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID
import kotliquery.Row

data class Relationship(
    val id: UUID,
    val userId: UUID,
    val partnerId: UUID,
    val otherRelationshipId: UUID,
    val nickname: String?,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_RELATIONSHIP,
    val notificationThreshold: Int = NOTIFICATION_THRESHOLD,
    val createdAt: Instant = now(),
    val updatedAt: Instant?,
    val deactivatedAt: Instant?,
    val deletedAt: Instant?,
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
        row.instant("created_at"),
        row.instantOrNull("updated_at"),
        row.instantOrNull("deactivated_at"),
        row.instantOrNull("deleted_at"),
    )
}
