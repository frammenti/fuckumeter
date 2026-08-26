package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.Deactivable.Status
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: UUID,
    val name: String,
    val status: Status,
)
