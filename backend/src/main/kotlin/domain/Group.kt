package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class Group(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val updatedBy: UUID?,
    val createdAt: Instant = now(),
    val updatedAt: Instant?,
)
