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

    fun inviteUser(
        userId: UUID,
        groupId: UUID?, // not sure if it really makes sense
    ): InviteResponse {
        val latest =
            repository
                .findLatestByUser(
                    userId,
                    InviteType.INVITE_USER,
                )
                .expect<InviteUser>()

        val previousStatus = statusOf(latest)

        if (previousStatus == InviteStatus.ACTIVE)
            return InviteResponse(
                latest!!.code,
                latest.expiresAt,
                previousStatus,
            )

        val invite = InviteUser(createdBy = userId, groupId = groupId)
        repository.insert(invite)

        return InviteResponse(invite.code, invite.expiresAt, previousStatus)
    }

    fun joinGroup(userId: UUID, groupId: UUID): InviteResponse {
        val latest = repository.findLatestForGroup(groupId).expect<JoinGroup>()

        val previousStatus = statusOf(latest)

        if (previousStatus == InviteStatus.ACTIVE)
            return InviteResponse(
                latest!!.code,
                latest.expiresAt,
                previousStatus,
            )

        val invite = JoinGroup(createdBy = userId, groupId = groupId)
        repository.insert(invite)

        return InviteResponse(invite.code, invite.expiresAt, previousStatus)
    }

    fun linkDevice(
        userId: UUID,
        deviceName: String, // a default is always provided by the client app
    ): InviteResponse {
        val latest =
            repository
                .findLatestByUser(
                    userId,
                    InviteType.LINK_DEVICE,
                )
                .expect<LinkDevice>()

        val previousStatus = statusOf(latest)

        if (previousStatus == InviteStatus.ACTIVE)
            return InviteResponse(
                latest!!.code,
                latest.expiresAt,
                previousStatus,
            )

        val invite = LinkDevice(createdBy = userId, deviceName = deviceName)
        repository.insert(invite)

        return InviteResponse(invite.code, invite.expiresAt, previousStatus)
    }

    fun recovery(
        userId: UUID,
        recoveryRequestId:
            Int, // it always gets called after checking the recovery request
    ): InviteResponse {
        val latest =
            repository
                .findLatestByUser(
                    userId,
                    InviteType.RECOVERY,
                )
                .expect<Recovery>()

        val previousStatus = statusOf(latest)

        if (previousStatus == InviteStatus.ACTIVE)
            return InviteResponse(
                latest!!.code,
                latest.expiresAt,
                previousStatus,
            )

        val invite =
            Recovery(
                createdBy = userId,
                recoveryRequestId = recoveryRequestId,
            )
        repository.insert(invite)

        return InviteResponse(invite.code, invite.expiresAt, previousStatus)
    }
}
