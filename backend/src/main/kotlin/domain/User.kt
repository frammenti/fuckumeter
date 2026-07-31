package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID
import kotliquery.Row

/** User class */
class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdAt: Instant = now(),
    val updatedAt: Instant? = null,
    val deactivatedAt: Instant? = null,
    val deletedAt: Instant? = null,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("name"),
        row.instant("created_at"),
        row.instantOrNull("updated_at"),
        row.instantOrNull("deactivated_at"),
        row.instantOrNull("deleted_at"),
    )
}
