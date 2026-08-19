package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Membership
import dev.frammenti.fuckumeter.dto.GroupResponse
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.repository.GroupRepository
import dev.frammenti.fuckumeter.shared.Time.now
import java.util.UUID

class GroupService(private val groups: GroupRepository) {
    suspend fun get(id: UUID, userId: UUID): GroupResponse {
        val group =
            groups.findForUser(id, userId)
                ?: throw ResourceNotFoundException("group")
        val members = groups.findMembersForUser(id, userId)
        return GroupResponse(id, group.name, members)
    }

    suspend fun getName(id: UUID, userId: UUID): String {
        val group =
            groups.findForUser(id, userId)
                ?: throw ResourceNotFoundException("group")
        return group.name
    }

    suspend fun addMember(userId: UUID, groupId: UUID) {
        val membership = Membership(userId, groupId, joinedAt = now())
        TODO()
    }
}
