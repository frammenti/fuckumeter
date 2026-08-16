package dev.frammenti.fuckumeter.view

import java.util.UUID
import kotliquery.Row

data class GroupMember(
    val id: UUID,
    val displayName: String,
    val relationshipId: UUID?,
    val userActive: Boolean,
)
