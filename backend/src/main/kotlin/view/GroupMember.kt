package dev.frammenti.fuckumeter.view

import dev.frammenti.fuckumeter.dto.UUID
import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val id: UUID,
    val displayName: String,
    val relationshipId: UUID?,
    val userActive: Boolean,
)
