package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class RelationshipRequest(
    val partnerId: UUID,
    val nickname: String? = null,
)
