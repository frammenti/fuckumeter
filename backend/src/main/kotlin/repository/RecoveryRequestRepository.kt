package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.InternalId.WithId
import dev.frammenti.fuckumeter.domain.RecoveryRequest
import dev.frammenti.fuckumeter.extensions.expectNotNull
import dev.frammenti.fuckumeter.extensions.expectOne
import java.util.UUID
import kotliquery.Row

class RecoveryRequestRepository(database: Database) : Repository(database) {
    private fun RecoveryRequest.toParams() =
        arrayOf(
            "relationship_id" to relationshipId,
            "invite_id" to inviteId,
            "created_at" to createdAt,
            "revoked_at" to revokedAt,
            "revoked_by_partner_at" to revokedByPartnerAt,
        )

    private fun Row.toRecoveryRequest() =
        RecoveryRequest(
            relationshipId = uuid("relationship_id"),
            inviteId = longOrNull("invite_id"),
            createdAt = instant("created_at"),
            revokedAt = instantOrNull("revoked_at"),
            revokedByPartnerAt = instantOrNull("revoked_by_partner_at"),
        )

    private fun Row.toRecoveryRequestWithId(): WithId<RecoveryRequest> {
        val request = toRecoveryRequest()
        return WithId(request, long("id"))
    }

    suspend fun findLatestByUser(userId: UUID): WithId<RecoveryRequest>? =
        session {
            single(
                sql(
                    """
                    SELECT q.*
                    FROM recovery_requests q JOIN relationships r
                    ON q.relationship_id = r.id
                    WHERE r.user_id = :user_id
                      AND q.revoked_at IS NULL
                    ORDER BY id DESC
                    LIMIT 1;
                    """,
                    "user_id" to userId,
                )
            ) { row ->
                row.toRecoveryRequestWithId()
            }
        }

    suspend fun insert(request: RecoveryRequest): Long = session {
        updateAndReturnGeneratedKey(
                sql(
                    """
                    INSERT INTO recovery_requests (
                        relationship_id, invite_id, created_at,
                        revoked_at, revoked_by_partner_at,
                    )
                    VALUES (
                        :relationship_id, :invite_id, :created_at,
                        :revoked_at, :revoked_by_partner_at
                    );
                    """,
                    *request.toParams(),
                )
            )
            .expectNotNull()
    }

    suspend fun setInviteId(requestId: Long, inviteId: Long) = session {
        update(
                sql(
                    """
                    UPDATE recovery_requests
                    SET invite_id = :invite_id
                    WHERE id = :id
                      AND invite_id IS NULL;
                    """,
                    "id" to requestId,
                    "invite_id" to inviteId,
                )
            )
            .expectOne()
    }
}
