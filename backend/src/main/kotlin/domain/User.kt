package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.shared.Time.now
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdAt: Instant = now(),
    val updatedAt: Instant? = null,
    override val deactivatedAt: Instant? = null,
    override val deletedAt: Instant? = null,
) : Deactivable
