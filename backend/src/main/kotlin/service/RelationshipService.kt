package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Relationship
import dev.frammenti.fuckumeter.dto.RelationshipResponse
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.repository.RelationshipRepository
import java.util.UUID

class RelationshipService(private val relationships: RelationshipRepository) {
    suspend fun get(id: UUID): RelationshipResponse {
        val relationship =
            relationships.find(id)
                ?: throw ResourceNotFoundException("relationship")

        return RelationshipResponse(relationship)
    }

    suspend fun getPartner(relationshipId: UUID): UUID {
        return relationships.findPartner(relationshipId)
            ?: throw ResourceNotFoundException("relationship")
    }

    // TODO: What about partner approval?
    suspend fun createPair(userIds: Pair<UUID, UUID>): RelationshipResponse {
        val ids = UUID.randomUUID() to UUID.randomUUID()
        val relationship =
            Relationship(
                ids.first,
                userIds.first,
                userIds.second,
                ids.second,
            ) to
                Relationship(
                    ids.second,
                    userIds.second,
                    userIds.first,
                    ids.first,
                )

        relationships.transaction {
            relationships.insert(relationship.first)
            relationships.insert(relationship.second)
        }

        return RelationshipResponse(relationship.first)
    }
}
