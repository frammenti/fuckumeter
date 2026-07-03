package dev.frammenti.fuckumeter.database.users

import dev.frammenti.fuckumeter.database.Database
import dev.frammenti.fuckumeter.database.sql
import kotliquery.queryOf
import kotliquery.sessionOf
import java.util.UUID

class UserRepository {
    fun find(id: UUID): User? {
        return Database.session {
            single(
                sql(
                    """
                    SELECT *
                    FROM users
                    WHERE id = :id
                    """,
                    "id" to id.toString()
                ),
                toUser
            )
        }
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
                    "id" to id.toString()
                ).map(toUser).asSingle
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
                mapOf("id" to id.toString())
            )
                .map(toUser).asSingle
        )
    }
}
