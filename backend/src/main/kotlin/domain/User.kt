package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdAt: Instant = now(),
    val updatedAt: Instant? = null,
    val deactivatedAt: Instant? = null,
    val deletedAt: Instant? = null,
) {
    enum class UserStatus {
        ACTIVE,
        DEACTIVATED,
        DELETED,
    }

    fun status(): UserStatus {
        return when {
            this.deletedAt != null -> UserStatus.DELETED
            this.deactivatedAt != null -> UserStatus.DEACTIVATED
            else -> UserStatus.ACTIVE
        }
    }
}
