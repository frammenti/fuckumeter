package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Invite.InviteType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface InviteRequest {
    val type: InviteType

    @Serializable
    @SerialName("invite-user")
    class InviteUserRequest : InviteRequest {
        override val type = InviteType.INVITE_USER
    }

    @Serializable
    @SerialName("join-group")
    data class JoinGroupRequest(
        val groupId: UUID,
    ) : InviteRequest {
        override val type = InviteType.JOIN_GROUP
    }

    @Serializable
    @SerialName("link-device")
    class LinkDeviceRequest : InviteRequest {
        override val type = InviteType.LINK_DEVICE
    }

    @Serializable
    @SerialName("recovery")
    data class RecoveryRequest(
        val recoveryRequestId: Int,
    ) : InviteRequest {
        override val type: InviteType = InviteType.RECOVERY
    }
}

