package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.SHARE_RELATIONSHIPS
import kotliquery.Row
import java.time.OffsetDateTime
import java.util.UUID

class Membership(
    val id: UUID,
    val userId: UUID,
    val groupId: UUID,
    val shareRelationships: Boolean = SHARE_RELATIONSHIPS,
    val joinedAt: OffsetDateTime?,
    val leftAt: OffsetDateTime?,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.uuid("user_id"),
        row.uuid("group_id"),
        row.boolean("share_relationships"),
        row.offsetDateTimeOrNull("joined_at"),
        row.offsetDateTimeOrNull("left_at"),
    )
}
