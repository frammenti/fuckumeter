package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val name: String,
    val deviceName: String,
)
