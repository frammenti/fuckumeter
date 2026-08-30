package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Deactivable
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.dto.CredentialsResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse
import dev.frammenti.fuckumeter.dto.RedemptionResponse.*
import dev.frammenti.fuckumeter.dto.UserResponse
import dev.frammenti.fuckumeter.exceptions.AlreadyAuthenticatedException
import dev.frammenti.fuckumeter.exceptions.ConcurrentUpdateException
import dev.frammenti.fuckumeter.exceptions.InvalidCodeException
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.repository.InviteRepository
import java.util.UUID

class RedemptionService(
    private val invites: InviteRepository,
    private val userService: UserService,
    private val deviceService: DeviceService,
    private val groupService: GroupService,
    private val relationshipService: RelationshipService,
) {
    private data class RedemptionContext(
        val inviteId: Long,
        val creator: UserResponse,
    )

    private sealed class RedeemingUser {
        open suspend fun UserService.create():
            Pair<UUID, CredentialsResponse?> =
            error("Pending requests cannot create an identity")

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
        ): CredentialsResponse =
            error("Pending requests cannot create a device")

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

    private inline fun <T> catchObfuscate(block: () -> T): T =
        try {
            block()
        } catch (_: ResourceNotFoundException) {
            throw InvalidCodeException()
        }

    private inline fun <T> catchConcurrent(block: () -> T): T =
        try {
            block()
        } catch (_: NoSuchElementException) {
            throw ConcurrentUpdateException("invite", "redeemed")
        }

    private suspend fun validate(
        code: String
    ): Pair<RedemptionContext, Invite> {
        val (invite, id) =
            invites.findByCode(code) ?: throw InvalidCodeException()
        if (invite.status() != Status.ACTIVE) throw InvalidCodeException()

        val creator = catchObfuscate { userService.get(invite.createdBy) }
        if (creator.status != Deactivable.Status.ACTIVE)
            throw InvalidCodeException()

        return RedemptionContext(id, creator) to invite
    }

    private suspend fun redeemLinkDevice(
        context: RedemptionContext,
        device: RedeemingDevice,
    ): RedemptionResponse {
        val (inviteId, creator) = context

        if (device is RedeemingDevice.Pending)
            return PendingLinkDeviceRedemptionResponse(creator.name)

        val credentials = invites.transaction {
            // Perform the less expensive operation first,
            // everything is always rolled back
            catchConcurrent { invites.consume(inviteId, creator.id) }
            with(device) { deviceService.create(creator.id) }
        }

        return LinkDeviceRedemptionResponse(creator.name, credentials)
    }

    private suspend fun redeemRecovery(
        context: RedemptionContext,
        device: RedeemingDevice,
        userId: UUID,
    ): RedemptionResponse {
        val (inviteId) = context

        val user = catchObfuscate { userService.get(userId) }

        // We do not check user status because recovery must
        // be possible even for deactivated users

        if (device is RedeemingDevice.Pending)
            return PendingRecoveryRedemptionResponse(user.name)

        val credentials = invites.transaction {
            catchConcurrent { invites.consume(inviteId, user.id) }
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

                catchConcurrent { invites.consume(inviteId, userId) }

                // Let error bubble if relationship already exists
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

        val groupName = catchObfuscate {
            groupService.getName(groupId, creator.id)
        }

        return if (user is RedeemingUser.Pending)
            PendingJoinGroupRedemptionResponse(groupName)
        else
            invites.transaction {
                val (userId, credentials) = with(user) { userService.create() }

                catchConcurrent { invites.consume(inviteId, userId) }

                // Let error bubble if user is already part of the group
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
        val (context, invite) = validate(code)

        return when (invite) {
            is LinkDevice ->
                redeemLinkDevice(
                    context,
                    // Throws when authenticated (userId != null)
                    RedeemingDevice.resolve(
                        userId,
                        deviceName,
                    ),
                )

            is Recovery ->
                redeemRecovery(
                    context,
                    // Throws when authenticated (userId != null)
                    RedeemingDevice.resolve(
                        userId,
                        deviceName,
                    ),
                    invite.partnerId,
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
