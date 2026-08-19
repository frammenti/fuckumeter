package dev.frammenti.fuckumeter.view

import java.time.Instant
import java.util.UUID
import kotliquery.Row

data class GroupMembership(
    val groupId: UUID,
    val name: String,
    val createdAt: Instant,
    val joinedAt: Instant,
    val shareRelationships: Boolean,
)
