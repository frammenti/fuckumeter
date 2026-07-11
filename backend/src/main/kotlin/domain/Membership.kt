package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.SHARE_RELATIONSHIPS
import kotliquery.Row
import java.time.Instant
import java.util.UUID

class Membership(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val groupId: UUID,
    val shareRelationships: Boolean = SHARE_RELATIONSHIPS,
    val joinedAt: Instant?, // we do not initialize it in the constructor
    val leftAt: Instant?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.uuid("user_id"),
        row.uuid("group_id"),
        row.boolean("share_relationships"),
        row.instantOrNull("joined_at"),
        row.instantOrNull("left_at"),
    )
}
