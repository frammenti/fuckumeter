package fixtures

import dev.frammenti.fuckumeter.shared.Time.now
import fixtures.TestCrypto.hasher
import java.time.Instant
import java.util.UUID
import kotliquery.Row

object TestFixtures {
    private val database = TestDatabase.database

    fun <T> getProperty(
        table: String,
        column: String,
        id: Any,
        mapper: Row.(columnLabel: String) -> T,
    ): T? = database.session {
        single(
            database.sql(
                """
                SELECT $column
                FROM $table
                WHERE id = :id
                """,
                "id" to id,
            )
        ) { row ->
            row.mapper(column)
        }
    }

    fun getProperty(
        table: String,
        column: String,
        id: Any,
    ): String? = getProperty(table, column, id) { string(it) }

    fun insertUser(
        id: UUID = UUID.randomUUID(),
        name: String = "Test user",
        createdAt: Instant = now(),
        deactivatedAt: Instant? = null,
        deletedAt: Instant? = null,
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO users (id, name, created_at, deactivated_at, deleted_at)
                    VALUES (:id, :name, :created_at, :deactivated_at, :deleted_at);
                    """,
                    "id" to id,
                    "name" to name,
                    "created_at" to createdAt,
                    "deactivated_at" to deactivatedAt,
                    "deleted_at" to deletedAt,
                )
            )
        }
        return id
    }

    fun insertDevice(
        id: UUID = UUID.randomUUID(),
        userId: UUID = insertUser(),
        name: String = "Test device",
        notificationEnabled: Boolean = false,
        createdAt: Instant = now(),
        refreshToken: String = "token",
    ): UUID {
        database.session {
            update(
                database.sql(
                    """
                    INSERT INTO devices (
                        id, user_id, name, notification_enabled,
                        refresh_token_hash, created_at
                    )
                    VALUES (
                        :id, :user_id, :name, :notification_enabled,
                        :refresh_token_hash, :created_at
                    );
                    """,
                    "id" to id,
                    "user_id" to userId,
                    "name" to name,
                    "notification_enabled" to notificationEnabled,
                    "refresh_token_hash" to hasher.hash(refreshToken),
                    "created_at" to createdAt,
                )
            )
        }
        return id
    }
}
