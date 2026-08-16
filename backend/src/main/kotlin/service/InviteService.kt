package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.domain.User.UserStatus
import dev.frammenti.fuckumeter.dto.InviteResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse.*
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
    private fun <I : Invite> createInvite(
        userId: UUID,
        factory: () -> I,
    ): InviteResponse {
        val new = WithCode(factory())

        val previous =
            invites
                .findLatestByUser(userId, new.invite.type)
                .expect<WithCode<I>>()

        val previousStatus = previous?.invite?.status() ?: InviteStatus.NONE

        if (previousStatus == InviteStatus.ACTIVE) {
            return InviteResponse(
                previous!!.code,
                previous.invite.expiresAt,
                previousStatus,
            )
        }

        invites.insert(new)

        return InviteResponse(
            new.code,
            new.invite.expiresAt,
            previousStatus,
        )
    }

    fun inviteUser(userId: UUID) =
        createInvite(userId) {
            InviteUser(createdBy = userId)
        }

    fun joinGroup(userId: UUID, groupId: UUID) =
        createInvite(userId) {
            JoinGroup(
                createdBy = userId,
                groupId = groupId,
            )
        }

    fun linkDevice(userId: UUID) =
        createInvite(userId) {
            LinkDevice(createdBy = userId)
        }

    fun recovery(
        userId: UUID,
        recoveryRequestId:
            Int, // it always gets called after checking the recovery request
    ) =
        createInvite(userId) {
            Recovery(createdBy = userId, recoveryRequestId = recoveryRequestId)
        }

    fun redeem(
        code: String,
        principal: UserDevicePrincipal?,
        username: String?,
        deviceName: String?,
    ): RedemptionResponse {
        // Validation with opaque response
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

        when (invite) {
            is LinkDevice -> {
                if (principal != null) throw AlreadyAuthenticatedException()

                if (deviceName == null)
                    return PendingLinkDeviceRedemptionResponse(creator.name)

                val credentials = deviceService.create(creator.id, deviceName)

                invites.consume(id, creator.id)
                return LinkDeviceRedemptionResponse(creator.name, credentials)
            }

            is Recovery -> {
                if (principal != null) throw AlreadyAuthenticatedException()

                val userId =
                    invites.findRecoveryTarget(invite.recoveryRequestId)
                        ?: throw InvalidCodeException()

                val user =
                    try {
                        userService.get(userId)
                    } catch (_: ResourceNotFoundException) {
                        throw InvalidCodeException()
                    }

                if (deviceName == null)
                    return PendingRecoveryRedemptionResponse(user.name)

                val credentials = deviceService.create(user.id, deviceName)

                invites.consume(id, user.id)
                return RecoveryRedemptionResponse(user.name, credentials)
            }

            is InviteUser -> {
                val credentials =
                    if (principal == null) {
                        if (username == null || deviceName == null)
                            return PendingInviteUserRedemptionResponse(
                                partnerName = creator.name
                            )
                        userService.create(username, deviceName)
                    } else null

                val userId = principal?.userId ?: credentials!!.userId

                val (relationshipId) =
                    relationshipService.createPair(Pair(userId, creator.id))

                invites.consume(id, userId)
                return InviteUserRedemptionResponse(
                    creator.name,
                    relationshipId,
                    credentials,
                )
            }

            is JoinGroup -> {
                val group =
                    try {
                        groupService.get(invite.groupId)
                    } catch (_: ResourceNotFoundException) {
                        throw InvalidCodeException()
                    }

                // TODO: Check if creator.id is among group members?
                // Get could return the members userId list as well

                val credentials =
                    if (principal == null) {
                        if (username == null || deviceName == null)
                            return PendingJoinGroupRedemptionResponse(
                                groupName = group.name
                            )
                        userService.create(username, deviceName)
                    } else null

                val userId = principal?.userId ?: credentials!!.userId

                groupService.addMember(userId, group.id)

                invites.consume(id, userId)
                return JoinGroupRedemptionResponse(
                    group.name,
                    group.id,
                    credentials,
                )
            }
        }
    }
}
