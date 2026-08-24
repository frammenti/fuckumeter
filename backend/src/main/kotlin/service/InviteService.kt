package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.domain.User.UserStatus
import dev.frammenti.fuckumeter.dto.CredentialsResponse
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
    private suspend fun <I : Invite> getOrCreateInvite(
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
        getOrCreateInvite(userId) {
            InviteUser(createdBy = userId)
        }

    suspend fun joinGroup(userId: UUID, groupId: UUID) =
        getOrCreateInvite(userId) {
            JoinGroup(
                createdBy = userId,
                groupId = groupId,
            )
        }

    suspend fun linkDevice(userId: UUID) =
        getOrCreateInvite(userId) {
            LinkDevice(createdBy = userId)
        }

    suspend fun recovery(
        userId: UUID,
        relationshipId: UUID?,
    ) =
        getOrCreateInvite(userId) {
            Recovery(createdBy = userId, relationshipId = relationshipId)
        }

    // Redemption

    private data class RedemptionContext(
        val inviteId: Int,
        val creator: UserResponse,
    )

    private sealed class RedeemingUser {
        open suspend fun UserService.create():
            Pair<UUID, CredentialsResponse?> =
            error("Pending has no identity to create")

        data class Existing(val id: UUID) : RedeemingUser() {
            override suspend fun UserService.create():
                Pair<UUID, CredentialsResponse?> = id to null
        }

        data class New(val name: String, val deviceName: String) :
            RedeemingUser() {
            override suspend fun UserService.create():
                Pair<UUID, CredentialsResponse?> =
                create(name, deviceName).let { it.userId to it }
        }

        data object Pending : RedeemingUser()

        companion object {
            fun resolve(
                userId: UUID?,
                username: String?,
                deviceName: String?,
            ): RedeemingUser =
                when {
                    userId != null -> Existing(userId)
                    username != null && deviceName != null ->
                        New(username, deviceName)
                    else -> Pending
                }
        }
    }

    private sealed class RedeemingDevice {
        open suspend fun DeviceService.create(
            userId: UUID
        ): CredentialsResponse = error("Pending has no device to create")

        data class New(val deviceName: String) : RedeemingDevice() {
            override suspend fun DeviceService.create(
                userId: UUID
            ): CredentialsResponse = create(userId, deviceName)
        }

        data object Pending : RedeemingDevice()

        companion object {
            fun resolve(
                userId: UUID?,
                deviceName: String?,
            ): RedeemingDevice =
                when {
                    userId != null -> throw AlreadyAuthenticatedException()
                    deviceName != null -> New(deviceName)
                    else -> Pending
                }
        }
    }

    private suspend fun validate(
        code: String
    ): Pair<Invite, RedemptionContext> {
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

        return invite to RedemptionContext(id, creator)
    }

    private suspend fun redeemLinkDevice(
        context: RedemptionContext,
        device: RedeemingDevice,
    ): RedemptionResponse {
        val (inviteId, creator) = context

        if (device !is RedeemingDevice.New)
            return PendingLinkDeviceRedemptionResponse(creator.name)

        val credentials = invites.transaction {
            invites.consume(inviteId, creator.id)
            with(device) { deviceService.create(creator.id) }
        }

        return LinkDeviceRedemptionResponse(creator.name, credentials)
    }

    private suspend fun redeemRecovery(
        context: RedemptionContext,
        device: RedeemingDevice,
        relationshipId: UUID,
    ): RedemptionResponse {
        val (inviteId) = context

        val user =
            try {
                val partnerId = relationshipService.getPartner(relationshipId)
                userService.get(partnerId)
            } catch (_: ResourceNotFoundException) {
                throw InvalidCodeException()
            }

        if (device is RedeemingDevice.Pending)
            return PendingRecoveryRedemptionResponse(user.name)

        val credentials = invites.transaction {
            invites.consume(inviteId, user.id)
            with(device) { deviceService.create(user.id) }
        }

        return RecoveryRedemptionResponse(user.name, credentials)
    }

    private suspend fun redeemInviteUser(
        context: RedemptionContext,
        user: RedeemingUser,
    ): RedemptionResponse {
        val (inviteId, creator) = context

        return if (user is RedeemingUser.Pending)
            PendingInviteUserRedemptionResponse(partnerName = creator.name)
        else
            invites.transaction {
                val (userId, credentials) = with(user) { userService.create() }

                invites.consume(inviteId, userId)

                val (relationshipId) =
                    relationshipService.createPair(userId to creator.id)

                InviteUserRedemptionResponse(
                    creator.name,
                    relationshipId,
                    credentials,
                )
            }
    }

    private suspend fun redeemJoinGroup(
        context: RedemptionContext,
        user: RedeemingUser,
        groupId: UUID,
    ): RedemptionResponse {
        val (inviteId, creator) = context

        val groupName =
            try {
                groupService.getName(groupId, creator.id)
            } catch (_: ResourceNotFoundException) {
                throw InvalidCodeException()
            }

        return if (user is RedeemingUser.Pending)
            PendingJoinGroupRedemptionResponse(groupName)
        else
            invites.transaction {
                val (userId, credentials) = with(user) { userService.create() }

                invites.consume(inviteId, userId)

                groupService.addMember(userId, groupId)

                JoinGroupRedemptionResponse(
                    groupName,
                    groupId,
                    credentials,
                )
            }
    }

    suspend fun redeem(
        code: String,
        userId: UUID?,
        username: String?,
        deviceName: String?,
    ): RedemptionResponse {
        val (invite, context) = validate(code)

        return when (invite) {
            is LinkDevice ->
                redeemLinkDevice(
                    context,
                    RedeemingDevice.resolve(
                        userId,
                        deviceName,
                    ),
                )

            is Recovery ->
                redeemRecovery(
                    context,
                    RedeemingDevice.resolve(
                        userId,
                        deviceName,
                    ),
                    invite.relationshipId,
                )

            is InviteUser ->
                redeemInviteUser(
                    context,
                    RedeemingUser.resolve(
                        userId,
                        username,
                        deviceName,
                    ),
                )
            is JoinGroup ->
                redeemJoinGroup(
                    context,
                    RedeemingUser.resolve(
                        userId,
                        username,
                        deviceName,
                    ),
                    invite.groupId,
                )
        }
    }
}
