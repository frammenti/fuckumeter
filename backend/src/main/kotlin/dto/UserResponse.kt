package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.domain.User.UserStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: UUID,
    val name: String,
    val status: UserStatus
)
