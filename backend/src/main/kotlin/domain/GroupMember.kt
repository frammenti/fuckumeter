package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.dto.UUIDSerializer
import kotlinx.serialization.Serializable
import kotliquery.Row

typealias UUID = @Serializable(with = UUIDSerializer::class) java.util.UUID

@Serializable
class GroupMember(
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
