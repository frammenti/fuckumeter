package dev.frammenti.fuckumeter.service

import dev.frammenti.fuckumeter.domain.Relationship
import dev.frammenti.fuckumeter.dto.RelationshipResponse
import dev.frammenti.fuckumeter.exceptions.ResourceNotFoundException
import dev.frammenti.fuckumeter.repository.RelationshipRepository
import java.util.UUID

class RelationshipService(private val relationships: RelationshipRepository) {
    suspend fun get(id: UUID): RelationshipResponse =
        relationships.find(id)?.let { RelationshipResponse(it) }
            ?: throw ResourceNotFoundException("relationship")

    suspend fun getByPartners(
        userId: UUID,
        partnerId: UUID,
    ): RelationshipResponse =
        relationships.findByPartners(userId, partnerId)?.let {
            RelationshipResponse(it)
        } ?: throw ResourceNotFoundException("relationship")

    suspend fun getPartner(relationshipId: UUID): UUID =
        relationships.findPartner(relationshipId)
            ?: throw ResourceNotFoundException("relationship")

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
