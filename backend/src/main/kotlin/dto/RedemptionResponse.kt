package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Invite.Type
import dev.frammenti.fuckumeter.domain.Invite.RedemptionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface RedemptionResponse {
    val type: Type
    val status: RedemptionStatus
    val credentials: CredentialsResponse?

    @Serializable
    sealed class PendingRedemptionResponse(
        override val type: Type,
        override val status: RedemptionStatus,
    ) : RedemptionResponse {
        override val credentials = null
    }

    @Serializable
    sealed class CompletedRedemptionResponse(override val type: Type) :
        RedemptionResponse {
        override val status = RedemptionStatus.COMPLETED
    }

    @Serializable
    @SerialName("pending-invite-user-redemption")
    data class PendingInviteUserRedemptionResponse(val partnerName: String) :
        PendingRedemptionResponse(
            Type.INVITE_USER,
            RedemptionStatus.REQUIRES_USER,
        )

    @Serializable
    @SerialName("pending-join-group-redemption")
    data class PendingJoinGroupRedemptionResponse(val groupName: String) :
        PendingRedemptionResponse(
            Type.JOIN_GROUP,
            RedemptionStatus.REQUIRES_USER,
        )

    @Serializable
    @SerialName("pending-link-device-redemption")
    data class PendingLinkDeviceRedemptionResponse(val username: String) :
        PendingRedemptionResponse(
            Type.LINK_DEVICE,
            RedemptionStatus.REQUIRES_DEVICE,
        )

    @Serializable
    @SerialName("pending-recovery-redemption")
    data class PendingRecoveryRedemptionResponse(val username: String) :
        PendingRedemptionResponse(
            Type.RECOVERY,
            RedemptionStatus.REQUIRES_DEVICE,
        )

    @Serializable
    @SerialName("invite-user-redemption")
    data class InviteUserRedemptionResponse(
        val partnerName: String,
        val relationshipId: UUID,
        override val credentials: CredentialsResponse?,
    ) : CompletedRedemptionResponse(Type.INVITE_USER)

    @Serializable
    @SerialName("join-group-redemption")
    data class JoinGroupRedemptionResponse(
        val groupName: String,
        val groupId: UUID,
        override val credentials: CredentialsResponse?,
    ) : CompletedRedemptionResponse(Type.JOIN_GROUP)

    @Serializable
    @SerialName("link-device-redemption")
    data class LinkDeviceRedemptionResponse(
        val username: String,
        override val credentials: CredentialsResponse,
    ) : CompletedRedemptionResponse(Type.LINK_DEVICE)

    @Serializable
    @SerialName("recovery-redemption")
    data class RecoveryRedemptionResponse(
        val username: String,
        override val credentials: CredentialsResponse,
    ) : CompletedRedemptionResponse(Type.RECOVERY)
}
