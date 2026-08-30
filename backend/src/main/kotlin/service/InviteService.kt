package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Deactivable
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.domain.RecoveryRequest
import dev.frammenti.fuckumeter.dto.InviteResponse
import dev.frammenti.fuckumeter.exceptions.*
import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.repository.RecoveryRequestRepository
import java.util.UUID

class InviteService(
    private val invites: InviteRepository,
    private val recoveryRequests: RecoveryRequestRepository,
    private val relationshipService: RelationshipService,
) {
    private suspend fun <I : Invite> create(
        previous: WithCode<I>?,
        factory: () -> I,
    ): InviteResponse {
        val previousStatus = previous?.invite?.status() ?: Status.NONE

        if (previousStatus == Status.ACTIVE) {
            return InviteResponse(
                previous!!.code,
                previous.invite.expiresAt,
                previousStatus,
            )
        }

        val new = WithCode(factory())

        invites.insert(new)

        return InviteResponse(
            new.code,
            new.invite.expiresAt,
            previousStatus,
        )
    }

    suspend fun inviteUser(userId: UUID): InviteResponse {
        val latest = invites.findLatestByUser(userId, InviteUser.type)

        return create(latest) {
            InviteUser(createdBy = userId)
        }
    }

    suspend fun joinGroup(userId: UUID, groupId: UUID): InviteResponse {
        val latest = invites.findLatestByUser(userId, JoinGroup.type, groupId)

        return create(latest) {
            JoinGroup(
                createdBy = userId,
                groupId = groupId,
            )
        }
    }

    suspend fun linkDevice(userId: UUID): InviteResponse {
        val latest = invites.findLatestByUser(userId, LinkDevice.type)

        return create(latest) {
            LinkDevice(createdBy = userId)
        }
    }

    suspend fun recovery(
        userId: UUID,
        partnerId: UUID?,
    ): InviteResponse? {
        val latest = recoveryRequests.findByUser(userId)

        if (latest == null) {
            if (partnerId == null) throw MissingParameterException("partnerId")

            val relationship =
                relationshipService.getByPartners(
                    userId,
                    partnerId,
                ) // throws if not found

            if (relationship.status != Deactivable.Status.ACTIVE)
                throw PermissionDeniedException(
                    "You cannot create a recovery invite for a relationship that is not active"
                )

            try {
                recoveryRequests.insert(RecoveryRequest(userId, partnerId))
            } catch (_: IllegalStateException) {
                throw ConcurrentUpdateException(
                    "recovery request",
                    "created",
                )
            }

            return null // ok created
        } else {
            val (request, id) = latest

            if (partnerId != null && partnerId != request.partnerId) throw AnotherRecoveryInviteException(
                    request.partnerId
                )

            if (request.revokedByPartnerAt != null)
                throw InviteRevokedByPartnerException()

            if (request.inviteId == null) {
                if (request.shouldWait())
                    throw RecoveryWaitException(request.waitEndsAt)

                val new = WithCode(Recovery(userId, request.partnerId))

                invites.transaction {
                    val inviteId = invites.insert(new)
                    try {
                        recoveryRequests.setInviteId(
                            id,
                            inviteId,
                        ) // checks if invite_id is null
                    } catch (_: NoSuchElementException) {
                        throw ConcurrentUpdateException("invite", "created")
                    }
                }

                return InviteResponse(
                    new.code,
                    new.invite.expiresAt,
                    Status.NONE,
                )
            } else {
                val previous =
                    invites.find(request.inviteId)
                        ?: error(
                            "Recovery request $id references nonexistent invite ${request.inviteId}"
                        )

                when (previous.invite.status()) {
                    Status.EXPIRED -> throw InviteExpiredException()
                    Status.CONSUMED -> throw InviteConsumedException()
                    Status.REVOKED -> throw InviteRevokedException()
                    Status.ACTIVE ->
                        return InviteResponse(
                            previous.code,
                            previous.invite.expiresAt,
                            Status.ACTIVE,
                        )
                    Status.NONE ->
                        error(
                            "Invite ${request.inviteId} associated with recovery request $id has impossible status NONE"
                        )
                }
            }
        }
    }

    suspend fun revoke(
        userId: UUID,
        type: TypeOf<Invite>,
        groupId: UUID? = null,
    ) {
        try {
            invites.revoke(userId, type, groupId)
        } catch (_: NoSuchElementException) {
            throw ResourceNotFoundException("invite")
        }
    }

    suspend fun revokeRecoveryByUser(userId: UUID) =
        try {
            recoveryRequests.revokeByUser(userId)
        } catch (_: NoSuchElementException) {
            throw ResourceNotFoundException("invite")
        }

    suspend fun revokeRecoveryByPartner(partnerId: UUID) =
        try {
            recoveryRequests.revokeByPartner(partnerId)
        } catch (_: NoSuchElementException) {
            throw ResourceNotFoundException("invite")
        }
}
