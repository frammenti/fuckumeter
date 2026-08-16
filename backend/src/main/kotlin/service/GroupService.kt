package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Membership
import dev.frammenti.fuckumeter.dto.GroupResponse
import dev.frammenti.fuckumeter.repository.GroupRepository
import dev.frammenti.fuckumeter.shared.Time.now
import java.util.UUID

class GroupService(private val groups: GroupRepository) {
    fun get(id: UUID): GroupResponse {
        TODO()
    }

    fun addMember(userId: UUID, groupId: UUID) {
        val membership = Membership(userId, groupId, joinedAt = now())
    }
}
