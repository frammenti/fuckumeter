package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(
    val userId: UUID,
    val deviceId: UUID,
    val token: String,
    val refreshToken: String,
)
