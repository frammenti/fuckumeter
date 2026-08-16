package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_RELATIONSHIP
import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_THRESHOLD
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class Relationship(
    val id: UUID,
    val userId: UUID,
    val partnerId: UUID,
    val otherRelationshipId: UUID,
    val nickname: String? = null,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_RELATIONSHIP,
    val notificationThreshold: Int = NOTIFICATION_THRESHOLD,
    val createdAt: Instant = now(),
    val updatedAt: Instant? = null,
    val deactivatedAt: Instant? = null,
    val deletedAt: Instant? = null,
)
