package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

typealias UUID = @Serializable(with = UUIDSerializer::class) java.util.UUID

@Serializable
data class UsersResponse(
    val userId: UUID,
    val deviceId: UUID,
    val token: String,
    val refreshToken: String,
)
