package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.NOTIFICATION_ENABLED_DEVICE
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class Device(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val name: String,
    val notificationEnabled: Boolean = NOTIFICATION_ENABLED_DEVICE,
    val fcmToken: String? = null,
    val createdAt: Instant = now(),
    val lastSeenAt: Instant? = null,
)
