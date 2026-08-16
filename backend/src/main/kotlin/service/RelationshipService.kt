package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Relationship
import dev.frammenti.fuckumeter.dto.RelationshipResponse
import dev.frammenti.fuckumeter.repository.RelationshipRepository
import java.util.UUID

class RelationshipService(private val relationships: RelationshipRepository) {
    // TODO: What about partner approval?
    fun createPair(userIds: Pair<UUID, UUID>): RelationshipResponse {
        val ids = Pair(UUID.randomUUID(), UUID.randomUUID())
        val relationship =
            Pair(
                Relationship(
                    ids.first,
                    userIds.first,
                    userIds.second,
                    ids.second,
                ),
                Relationship(
                    ids.second,
                    userIds.second,
                    userIds.first,
                    ids.first,
                ),
            )

        relationships.transaction {
            relationships.insert(relationship.first)
            relationships.insert(relationship.second)
        }

        return RelationshipResponse(relationshipId = relationship.first.id)
    }
}
