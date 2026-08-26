package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Deactivable
import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.domain.RecoveryRequest
import dev.frammenti.fuckumeter.dto.InviteResponse
import dev.frammenti.fuckumeter.exceptions.*
import dev.frammenti.fuckumeter.extensions.expect
import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.repository.RecoveryRequestRepository
import java.util.UUID

class InviteService(
    private val invites: InviteRepository,
    private val recoveryRequests: RecoveryRequestRepository,
    private val relationshipService: RelationshipService,
) {
    private suspend fun <I : Invite> getOrCreateInvite(
        userId: UUID,
        factory: () -> I,
    ): InviteResponse {
        val invite = factory()

        if (invite is Recovery)
            error(
                "Recovery invites can only be generated via recovery requests"
            )

        val previous =
            invites.findLatestByUser(userId, invite.type).expect<WithCode<I>>()

        val previousStatus = previous?.invite?.status() ?: Status.NONE

        if (previousStatus == Status.ACTIVE) {
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
    ): InviteResponse? {
        val latest = recoveryRequests.findLatestByUser(userId)

        if (latest == null) {
            if (relationshipId == null)
                throw MissingParameterException("relationshipId")

            val relationship =
                relationshipService.get(relationshipId) // throws if not found

            if (relationship.userId != userId)
                throw ResourceNotFoundException(
                    "relationship"
                ) // opaque response

            if (relationship.status != Deactivable.Status.ACTIVE)
                throw PermissionDeniedException(
                    "You cannot create a recovery invite for a relationship that is not active"
                )

            try {
                recoveryRequests.insert(RecoveryRequest(relationshipId))
            } catch (_: IllegalStateException) {
                throw ConcurrentUpdateException(
                    "recovery request",
                    "created",
                )
            }

            return null // ok created
        } else {
            val (request, id) = latest

            if (
                relationshipId != null &&
                    relationshipId != request.relationshipId
            )
                throw AnotherRecoveryInviteException(request.relationshipId)

            if (request.revokedByPartnerAt != null)
                throw InviteRevokedByPartnerException()

            if (request.inviteId == null) {
                if (request.shouldWait())
                    throw RecoveryWaitException(request.waitEndsAt)

                val new = WithCode(Recovery(userId, request.relationshipId))

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
}
