package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class RedemptionRequest(
    val username: String? = null,
    val deviceName: String? = null,
)
