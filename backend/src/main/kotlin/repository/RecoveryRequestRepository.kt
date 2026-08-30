package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.InternalId.WithId
import dev.frammenti.fuckumeter.domain.RecoveryRequest
import dev.frammenti.fuckumeter.extensions.expectNotNull
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.extensions.insert
import java.util.UUID
import kotliquery.Row

class RecoveryRequestRepository(database: Database) : Repository(database) {
    private fun RecoveryRequest.toParams() =
        arrayOf(
            "user_id" to userId,
            "partner_id" to partnerId,
            "invite_id" to inviteId,
            "created_at" to createdAt,
            "revoked_at" to revokedAt,
            "revoked_by_partner_at" to revokedByPartnerAt,
        )

    private fun Row.toRecoveryRequest() =
        RecoveryRequest(
            userId = uuid("user_id"),
            partnerId = uuid("partner_id"),
            inviteId = longOrNull("invite_id"),
            createdAt = instant("created_at"),
            revokedAt = instantOrNull("revoked_at"),
            revokedByPartnerAt = instantOrNull("revoked_by_partner_at"),
        )

    private fun Row.toRecoveryRequestWithId(): WithId<RecoveryRequest> {
        val request = toRecoveryRequest()
        return WithId(request, long("id"))
    }

    suspend fun findByUser(userId: UUID): WithId<RecoveryRequest>? = session {
        single(
            sql(
                """
                    SELECT *
                    FROM recovery_requests
                    WHERE user_id = :user_id
                      AND revoked_at IS NULL;
                    """,
                "user_id" to userId,
            )
        ) { row ->
            row.toRecoveryRequestWithId()
        }
    }

    suspend fun insert(request: RecoveryRequest): Long = session {
        insert(
                sql(
                    """
                    INSERT INTO recovery_requests (
                        user_id, partner_id, invite_id,
                        created_at, revoked_at, revoked_by_partner_at,
                    )
                    VALUES (
                        :user_id, :partner_id, :invite_id,
                        :created_at, :revoked_at, :revoked_by_partner_at
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

    suspend fun revokeByUser(userId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE recovery_requests
                    SET revoked_at = now()
                    WHERE user_id = :userId
                      AND revoked_at IS NULL;
                    """,
                    "user_id" to userId,
                )
            )
            .expectOne()
    }

    suspend fun revokeByPartner(partnerId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE recovery_requests
                    SET revoked_by_partner_at = now()
                    WHERE partner_id = :partnerId
                      AND revoked_at IS NULL
                      AND revoked_by_partner_at IS NULL;
                    """,
                    "partner_id" to partnerId,
                )
            )
            .expectOne()
    }
}
