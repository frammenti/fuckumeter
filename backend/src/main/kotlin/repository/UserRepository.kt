package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.User
import dev.frammenti.fuckumeter.extensions.expectOne
import java.util.UUID
import kotliquery.Row

class UserRepository(database: Database) : Repository(database) {
    private fun User.toParams() =
        arrayOf(
            "id" to id,
            "name" to name,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "deactivated_at" to deactivatedAt,
            "deleted_at" to deletedAt,
        )

    private fun Row.toUser(): User =
        User(
            uuid("id"),
            string("name"),
            instant("created_at"),
            instantOrNull("updated_at"),
            instantOrNull("deactivated_at"),
            instantOrNull("deleted_at"),
        )

    suspend fun find(id: UUID): User? = session {
        single(
            sql(
                """
                SELECT *
                FROM users
                WHERE id = :id;
                """,
                "id" to id,
            )
        ) { row ->
            row.toUser()
        }
    }

    suspend fun insert(user: User) = session {
        update(
                sql(
                    """
                    INSERT INTO users (id, name, created_at)
                    VALUES (:id, :name, :created_at);
                    """,
                    *user.toParams(),
                )
            )
            .expectOne()
    }

    suspend fun rename(id: UUID, name: String) = session {
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

    suspend fun deactivate(id: UUID): Boolean = session {
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

    suspend fun reactivate(id: UUID): Boolean = session {
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

    suspend fun delete(id: UUID): Boolean = session {
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
