package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.InviteStatus
import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse(
    val code: String,
    val expiresAt: Instant,
    val previousStatus: InviteStatus,
)
