package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.InviteStatus
import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse<T : Invite>(
    val invite: T,
    val previousStatus: InviteStatus,
)
