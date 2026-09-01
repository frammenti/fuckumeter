package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.SHARE_RELATIONSHIPS
import java.time.Instant
import java.util.UUID

data class Membership(
    val userId: UUID,
    val groupId: UUID,
    val shareRelationships: Boolean = SHARE_RELATIONSHIPS,
    val joinedAt: Instant? = null, // we do not initialize it in the constructor
    val leftAt: Instant? = null,
) : InternalId
