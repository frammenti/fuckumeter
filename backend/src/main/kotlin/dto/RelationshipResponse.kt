package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Deactivable
import dev.frammenti.fuckumeter.domain.Relationship
import kotlinx.serialization.Serializable

@Serializable
data class RelationshipResponse(
    val relationshipId: UUID,
    val userId: UUID,
    val partnerId: UUID,
    val nickname: String?,
    val status: Deactivable.Status,
) {
    constructor(
        relationship: Relationship
    ) : this(
        relationship.id,
        relationship.userId,
        relationship.partnerId,
        relationship.nickname,
        relationship.status(),
    )
}
