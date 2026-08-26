package dev.frammenti.fuckumeter.domain

import java.time.Instant

interface Deactivable {
    val deletedAt: Instant?
    val deactivatedAt: Instant?

    enum class Status {
        ACTIVE,
        DEACTIVATED,
        DELETED,
    }

    fun status(): Status {
        return when {
            this.deletedAt != null -> Status.DELETED
            this.deactivatedAt != null -> Status.DEACTIVATED
            else -> Status.ACTIVE
        }
    }
}
