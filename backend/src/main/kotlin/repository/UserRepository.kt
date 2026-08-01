package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.extensions.expectOne
import java.util.UUID

class UserRepository(database: Database) : Repository(database) {
    private fun User.params() =
        arrayOf(
            "id" to id,
            "name" to name,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "deactivated_at" to deactivatedAt,
            "deleted_at" to deletedAt,
        )

    fun find(id: UUID): User? = session {
        single(
            sql(
                """
                SELECT *
                FROM users
                WHERE id = :id;
                """,
                "id" to id,
            ),
            ::User,
        )
    }

    fun insert(user: User) = session {
        update(
                sql(
                    """
                    INSERT INTO users (id, name, created_at)
                    VALUES (:id, :name, :created_at);
                    """,
                    *user.params(),
                )
            )
            .expectOne()
    }

    fun rename(id: UUID, name: String) = session {
        update(
                sql(
                    """
                    UPDATE users
                    SET name = :name,
                        updated_at = now()
                    WHERE id = :id;
                    """,
                    "id" to id,
                    "name" to name,
                )
            )
            .expectOne()
    }

    fun deactivate(id: UUID): Boolean = session {
        update(
            sql(
                """
                    UPDATE users
                    SET deactivated_at = now(),
                        updated_at = now()
                    WHERE id = :id
                      AND deactivated_at IS NULL
                      AND deleted_at IS NULL;
                    """,
                "id" to id,
            )
        ) == 1
    }

    fun reactivate(id: UUID): Boolean = session {
        update(
            sql(
                """
                    UPDATE users
                    SET deactivated_at = NULL,
                        updated_at = now()
                    WHERE id = :id
                      AND deactivated_at IS NOT NULL
                      AND deleted_at IS NULL;
                    """,
                "id" to id,
            )
        ) == 1
    }

    fun delete(id: UUID): Boolean = session {
        update(
            sql(
                """
                    UPDATE users
                    SET deleted_at = now()
                    WHERE id = :id
                      AND deactivated_at IS NOT NULL
                      AND deleted_at IS NULL;
                    """,
                "id" to id,
            )
        ) == 1
    }
}
