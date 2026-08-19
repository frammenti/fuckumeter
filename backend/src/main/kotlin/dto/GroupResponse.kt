package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.view.GroupMember
import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: UUID,
    val name: String,
    val members: List<GroupMember>,
)
