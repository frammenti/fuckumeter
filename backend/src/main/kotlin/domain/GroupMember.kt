package dev.frammenti.fuckumeter.domain

import java.util.UUID
import kotliquery.Row

data class GroupMember(
    val id: UUID,
    val displayName: String,
    val relationshipId: UUID?,
    val userActive: Boolean,
) {
    constructor(
        row: Row
    ) : this(
        row.uuid("id"),
        row.string("display_name"),
        row.uuidOrNull("relationship_id"),
        row.boolean("user_active"),
    )
}
