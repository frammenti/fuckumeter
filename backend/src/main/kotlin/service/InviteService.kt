package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.dto.InviteResponse
import dev.frammenti.fuckumeter.extensions.expect
import dev.frammenti.fuckumeter.repository.InviteRepository
import java.util.UUID

class InviteService(private val repository: InviteRepository) {
    fun statusOf(invite: Invite?): InviteStatus =
        invite?.status() ?: InviteStatus.NONE

    fun getInviteUser(
        userId: UUID,
        groupId: UUID?, // not sure if it really makes sense
    ): InviteResponse<InviteUser> {
        val latest =
            repository
                .findLatestByCreatorAndType(
                    userId,
                    InviteType.INVITE_USER,
                )
                .expect<InviteUser>()

        val previousStatus = statusOf(latest)

        val invite =
            if (previousStatus == InviteStatus.ACTIVE) latest!!
            else InviteUser(createdBy = userId, groupId = groupId)

        return InviteResponse(invite, previousStatus)
    }

    fun getJoinGroup(userId: UUID, groupId: UUID): InviteResponse<JoinGroup> {
        val latest = repository.findLatestForGroup(groupId).expect<JoinGroup>()

        val previousStatus = statusOf(latest)

        val invite =
            if (previousStatus == InviteStatus.ACTIVE) latest!!
            else JoinGroup(createdBy = userId, groupId = groupId)

        return InviteResponse(invite, previousStatus)
    }

    fun getLinkDevice(
        userId: UUID,
        deviceName: String, // a default is always provided by the client app
    ): InviteResponse<LinkDevice> {
        val latest =
            repository
                .findLatestByCreatorAndType(
                    userId,
                    InviteType.LINK_DEVICE,
                )
                .expect<LinkDevice>()

        val previousStatus = statusOf(latest)

        val invite =
            if (previousStatus == InviteStatus.ACTIVE) latest!!
            else LinkDevice(createdBy = userId, deviceName = deviceName)

        return InviteResponse(invite, previousStatus)
    }

    fun getRecovery(
        userId: UUID,
        recoveryRequestId:
            Int, // it always gets called after checking the recovery request
    ): InviteResponse<Recovery> {
        val latest =
            repository
                .findLatestByCreatorAndType(
                    userId,
                    InviteType.RECOVERY,
                )
                .expect<Recovery>()

        val previousStatus = statusOf(latest)

        val invite =
            if (previousStatus == InviteStatus.ACTIVE) latest!!
            else
                Recovery(
                    createdBy = userId,
                    recoveryRequestId = recoveryRequestId,
                )

        return InviteResponse(invite, previousStatus)
    }
}
