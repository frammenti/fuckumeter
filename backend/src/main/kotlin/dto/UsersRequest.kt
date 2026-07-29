package dev.frammenti.fuckumeter.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersRequest(
    val name: String,
    val deviceName: String,
)
