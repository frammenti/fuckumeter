package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.RECOVERY_WAIT
import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class RecoveryRequest(
    val userId: UUID,
    val partnerId: UUID,
    val inviteId: Long? = null,
    val createdAt: Instant = now(),
    val revokedAt: Instant? = null,
    val revokedByPartnerAt: Instant? = null,
) : InternalId {
    val waitEndsAt: Instant = createdAt.plus(RECOVERY_WAIT)

    fun shouldWait(): Boolean = now().isBefore(waitEndsAt)
}
