package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.db.sql
import dev.frammenti.fuckumeter.domain.User
import kotliquery.queryOf
import kotliquery.sessionOf
import java.util.UUID

class UserRepository {
    fun find(id: UUID): User? = Database.session {
        single(
            sql(
                """
                SELECT *
                FROM users
                WHERE id = :id
                """,
                "id" to id,
            ),
            ::User,
        )
    }

    fun find2(id: UUID): User? {
        return Database.session {
            run(
                sql(
                        """
                    SELECT *
                    FROM users
                    WHERE id = :id
                    """,
                        "id" to id.toString(),
                    )
                    .map(::User)
                    .asSingle
            )
        }
    }

    fun find3(id: UUID): User? {
        val session = sessionOf(Database.ds)

        return session.run(
            queryOf(
                    """
                SELECT *
                FROM users
                WHERE id = :id
                """,
                    mapOf("id" to id.toString()),
                )
                .map(::User)
                .asSingle
        )
    }
}
