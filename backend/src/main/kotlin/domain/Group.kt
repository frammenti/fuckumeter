package dev.frammenti.fuckumeter.domain

import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

class Group(
    val id: UUID,
    val name: String,
    val updatedBy: UUID?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("name"),
        row.uuidOrNull("updated_by_user_id"),
        row.offsetDateTime("created_at"),
        row.offsetDateTimeOrNull("updated_at"),
    )
}
