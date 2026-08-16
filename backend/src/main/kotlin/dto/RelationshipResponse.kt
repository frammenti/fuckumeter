package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class RelationshipResponse(
    val relationshipId: UUID,
)
