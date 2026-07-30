package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String,
)
