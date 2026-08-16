package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: UUID,
    val name: String,
    val members: List<UUID>
)
