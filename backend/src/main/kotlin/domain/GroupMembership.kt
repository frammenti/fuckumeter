package dev.frammenti.fuckumeter.domain

import java.time.Instant
import java.util.UUID
import kotliquery.Row

data class GroupMembership(
    val groupId: UUID,
    val name: String,
    val createdAt: Instant,
    val joinedAt: Instant?,
    val leftAt: Instant?,
    val shareRelationships: Boolean,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("name"),
        row.instant("created_at"),
        row.instantOrNull("joined_at"),
        row.instantOrNull("left_at"),
        row.boolean("share_relationships"),
    )
}
