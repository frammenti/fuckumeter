package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.domain.User.UserStatus
import dev.frammenti.fuckumeter.dto.InviteResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse.*
import dev.frammenti.fuckumeter.dto.UserResponse
import dev.frammenti.fuckumeter.exceptions.AlreadyAuthenticatedException
import dev.frammenti.fuckumeter.exceptions.InvalidCodeException
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.extensions.expect
import dev.frammenti.fuckumeter.repository.InviteRepository
import java.util.UUID

class InviteService(
    private val invites: InviteRepository,
    private val userService: UserService,
    private val deviceService: DeviceService,
    private val groupService: GroupService,
    private val relationshipService: RelationshipService,
) {
    private suspend fun <I : Invite> createOrRetrieveInvite(
        userId: UUID,
        factory: () -> I,
    ): InviteResponse {
        val invite = factory()

        val previous =
            invites.findLatestByUser(userId, invite.type).expect<WithCode<I>>()

        val previousStatus = previous?.invite?.status() ?: InviteStatus.NONE

        if (previousStatus == InviteStatus.ACTIVE) {
            return InviteResponse(
                previous!!.code,
                previous.invite.expiresAt,
                previousStatus,
            )
        }

        val new = WithCode(invite)

        invites.insert(new)

        return InviteResponse(
            new.code,
            new.invite.expiresAt,
            previousStatus,
        )
    }

    suspend fun inviteUser(userId: UUID) =
        createOrRetrieveInvite(userId) {
            InviteUser(createdBy = userId)
        }

    suspend fun joinGroup(userId: UUID, groupId: UUID) =
        createOrRetrieveInvite(userId) {
            JoinGroup(
                createdBy = userId,
                groupId = groupId,
            )
        }

    suspend fun linkDevice(userId: UUID) =
        createOrRetrieveInvite(userId) {
            LinkDevice(createdBy = userId)
        }

    suspend fun recovery(
        userId: UUID,
        relationshipId: UUID,
    ) =
        createOrRetrieveInvite(userId) {
            Recovery(createdBy = userId, relationshipId = relationshipId)
        }

    // Redemption

    private data class ValidInvite<out I : Invite>(
        val invite: I,
        val id: Int,
        val creator: UserResponse,
    )

    private suspend fun validate(code: String): ValidInvite<Invite> {
        val (invite, id) =
            invites.findByCode(code) ?: throw InvalidCodeException()
        if (invite.status() != InviteStatus.ACTIVE) throw InvalidCodeException()

        val creator =
            try {
                userService.get(invite.createdBy)
            } catch (_: ResourceNotFoundException) {
                throw InvalidCodeException()
            }
        if (creator.status != UserStatus.ACTIVE) throw InvalidCodeException()

        return ValidInvite(invite, id, creator)
    }

    private fun requireAnonymous(principal: UserDevicePrincipal?) {
        if (principal != null) throw AlreadyAuthenticatedException()
    }

    private suspend fun redeemLinkDevice(
        id: Int,
        principal: UserDevicePrincipal?,
        creator: UserResponse,
        deviceName: String?,
    ): RedemptionResponse {
        requireAnonymous(principal)

        if (deviceName == null)
            return PendingLinkDeviceRedemptionResponse(creator.name)

        val credentials = invites.transaction {
            invites.consume(id, creator.id)
            deviceService.create(creator.id, deviceName)
        }

        return LinkDeviceRedemptionResponse(creator.name, credentials)
    }

    private suspend fun redeemRecovery(
        id: Int,
        principal: UserDevicePrincipal?,
        relationshipId: UUID,
        deviceName: String?,
    ): RedemptionResponse {
        requireAnonymous(principal)

        val user =
            try {
                val partnerId = relationshipService.getPartner(relationshipId)
                userService.get(partnerId)
            } catch (_: ResourceNotFoundException) {
                throw InvalidCodeException()
            }

        if (deviceName == null)
            return PendingRecoveryRedemptionResponse(user.name)

        val credentials = invites.transaction {
            invites.consume(id, user.id)
            deviceService.create(user.id, deviceName)
        }

        return RecoveryRedemptionResponse(user.name, credentials)
    }

    private suspend fun redeemInviteUser(
        id: Int,
        principal: UserDevicePrincipal?,
        creator: UserResponse,
        username: String?,
        deviceName: String?,
    ): RedemptionResponse {
        if (principal == null && (username == null || deviceName == null))
            return PendingInviteUserRedemptionResponse(
                partnerName = creator.name
            )

        return invites.transaction {
            val credentials =
                if (principal == null) {
                    userService.create(username!!, deviceName!!)
                } else null

            val userId = principal?.userId ?: credentials!!.userId

            val (relationshipId) =
                relationshipService.createPair(userId to creator.id)

            invites.consume(id, userId)

            InviteUserRedemptionResponse(
                creator.name,
                relationshipId,
                credentials,
            )
        }
    }

    private suspend fun redeemJoinGroup(
        id: Int,
        principal: UserDevicePrincipal?,
        creator: UserResponse,
        groupId: UUID,
        username: String?,
        deviceName: String?,
    ): RedemptionResponse {
        val groupName =
            try {
                groupService.getName(groupId, creator.id)
            } catch (_: ResourceNotFoundException) {
                throw InvalidCodeException()
            }

        if (principal == null && (username == null || deviceName == null))
            return PendingJoinGroupRedemptionResponse(groupName)

        return invites.transaction {
            val credentials =
                if (principal == null) {
                    userService.create(username!!, deviceName!!)
                } else null

            val userId = principal?.userId ?: credentials!!.userId

            groupService.addMember(userId, groupId)
            invites.consume(id, userId)

            JoinGroupRedemptionResponse(
                groupName,
                groupId,
                credentials,
            )
        }
    }

    suspend fun redeem(
        code: String,
        principal: UserDevicePrincipal?,
        username: String?,
        deviceName: String?,
    ): RedemptionResponse {
        val (invite, id, creator) = validate(code)

        return when (invite) {
            is LinkDevice ->
                redeemLinkDevice(id, principal, creator, deviceName)

            is Recovery ->
                redeemRecovery(
                    id,
                    principal,
                    invite.relationshipId,
                    deviceName,
                )
            is InviteUser ->
                redeemInviteUser(id, principal, creator, username, deviceName)
            is JoinGroup ->
                redeemJoinGroup(
                    id,
                    principal,
                    creator,
                    invite.groupId,
                    username,
                    deviceName,
                )
        }
    }
}
