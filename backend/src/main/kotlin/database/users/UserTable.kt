package dev.frammenti.fuckumeter.database.users

import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val deactivatedAt: OffsetDateTime?,
    val deletedAt: OffsetDateTime?
)

val toUser: (Row) -> User = { row ->
    User(
        row.uuid("id"),
        row.string("name"),
        row.offsetDateTime("created_at"),
        row.offsetDateTimeOrNull("updated_at"),
        row.offsetDateTimeOrNull("deactivated_at"),
        row.offsetDateTimeOrNull("deleted_at")
    )
}
