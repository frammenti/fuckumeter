package dev.frammenti.fuckumeter.domain

import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

class User(
    val id: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val deactivatedAt: OffsetDateTime?,
    val deletedAt: OffsetDateTime?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("name"),
        row.offsetDateTime("created_at"),
        row.offsetDateTimeOrNull("updated_at"),
        row.offsetDateTimeOrNull("deactivated_at"),
        row.offsetDateTimeOrNull("deleted_at"),
    )
}
