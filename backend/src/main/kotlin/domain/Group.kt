package dev.frammenti.fuckumeter.domain

import kotliquery.Row
import java.time.Instant
import java.util.UUID

class Group(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val updatedBy: UUID?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("name"),
        row.uuidOrNull("updated_by_user_id"),
        row.instant("created_at"),
        row.instantOrNull("updated_at"),
    )
}
