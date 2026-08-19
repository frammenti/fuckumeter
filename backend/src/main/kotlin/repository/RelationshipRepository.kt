package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.Relationship
import dev.frammenti.fuckumeter.extensions.expectOne
import kotliquery.Row

class RelationshipRepository(database: Database) : Repository(database) {
    private fun Relationship.toParams() =
        arrayOf(
            "id" to id,
            "user_id" to userId,
            "partner_id" to partnerId,
            "other_relationship_id" to otherRelationshipId,
            "nickname" to nickname,
            "notification_enabled" to notificationEnabled,
            "notification_threshold" to notificationThreshold,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "deactivated_at" to deactivatedAt,
            "deleted_at" to deletedAt,
        )

    private fun Row.toRelationship() =
        Relationship(
            uuid("id"),
            uuid("user_id"),
            uuid("partner_id"),
            uuid("other_relationship_id"),
            stringOrNull("nickname"),
            boolean("notification_enabled"),
            int("notification_threshold"),
            instant("created_at"),
            instantOrNull("updated_at"),
            instantOrNull("deactivated_at"),
            instantOrNull("deleted_at"),
        )

    // Must be used in a transaction of a pair of relationships
    suspend fun insert(relationship: Relationship) = session {
        update(
                sql(
                    """
                    INSERT INTO relationships (
                        id, user_id, partner_id, other_relationship_id,
                        nickname, notification_enabled, notification_threshold,
                        created_at, updated_at, deactivated_at, deleted_at
                    )
                    VALUES (
                        :id, :user_id, :partner_id, :other_relationship_id,
                        :nickname, :notification_enabled, :notification_threshold,
                        :created_at, :updated_at, :deactivated_at, :deleted_at
                    );
                    """,
                    *relationship.toParams(),
                )
            )
            .expectOne()
    }
}
