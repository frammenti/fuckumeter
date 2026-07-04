package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.db.sql
import dev.frammenti.fuckumeter.domain.Invite
import java.util.UUID

class InviteRepository {
    fun getInvitesByUser(userId: UUID): List<Invite> = Database.session {
        list(
            sql(
                """
                SELECT *
                FROM invites
                WHERE created_by_user_id = :user_id
                """,
                "user_id" to userId,
            ),
            Invite::factory,
        )
    }
}
