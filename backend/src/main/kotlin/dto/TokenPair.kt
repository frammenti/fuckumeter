package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenPair(
    val token: String,
    val refreshToken: String,
)
