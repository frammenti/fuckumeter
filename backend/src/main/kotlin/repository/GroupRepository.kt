package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.domain.Group
import dev.frammenti.fuckumeter.domain.Membership
import dev.frammenti.fuckumeter.extensions.expectNotNull
import dev.frammenti.fuckumeter.extensions.expectOne
import dev.frammenti.fuckumeter.view.GroupMember
import dev.frammenti.fuckumeter.view.GroupMembership
import java.util.UUID
import kotliquery.Row

class GroupRepository(database: Database) : Repository(database) {
    private fun Group.toParams() =
        arrayOf(
            "id" to id,
            "name" to name,
            "updated_by_user_id" to updatedBy,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
        )

    private fun Membership.toParams() =
        arrayOf(
            "user_id" to userId,
            "group_id" to groupId,
            "share_relationships" to shareRelationships,
            "joined_at" to joinedAt,
            "left_at" to leftAt,
        )

    private fun Row.toGroup() =
        Group(
            uuid("id"),
            string("name"),
            uuidOrNull("updated_by_user_id"),
            instant("created_at"),
            instantOrNull("updated_at"),
        )

    private fun Row.toMembership() =
        Membership(
            uuid("user_id"),
            uuid("group_id"),
            boolean("share_relationships"),
            instantOrNull("joined_at"),
            instantOrNull("left_at"),
        )

    private fun Row.toGroupMembership() =
        GroupMembership(
            uuid("id"),
            string("name"),
            instant("created_at"),
            instantOrNull("joined_at"),
            instantOrNull("left_at"),
            boolean("share_relationships"),
        )

    private fun Row.toGroupMember() =
        GroupMember(
            uuid("id"),
            string("display_name"),
            uuidOrNull("relationship_id"),
            boolean("user_active"),
        )

    fun find(id: UUID): Group? = session {
        single(
            sql(
                """
                    SELECT *
                    FROM groups
                    WHERE id = :id;
                    """,
                "id" to id,
            )
        ) { row ->
            row.toGroup()
        }
    }

    fun findAllForUser(userId: UUID): List<GroupMembership> = session {
        list(
            sql(
                """
                SELECT g.id, g.name, m.share_relationships,
                       g.created_at, m.joined_at, m.left_at
                FROM memberships m JOIN groups g
                ON m.group_id = g.id
                WHERE m.user_id = :user_id;
                """,
                "user_id" to userId,
            )
        ) { row ->
            row.toGroupMembership()
        }
    }

    // TODO: Add a way to mark pending direct invites
    fun findMembersForUser(userId: UUID, groupId: UUID): List<GroupMember> =
        session {
            list(
                sql(
                    """
                    SELECT
                        u.id,
                        COALESCE(r.nickname, u.name) AS display_name,
                        r.id AS relationship_id,
                        u.deactivated_at IS NULL AS user_active
                    FROM memberships m
                    JOIN users u
                        ON u.id = m.user_id
                    LEFT JOIN relationships r
                        ON r.partner_id = m.user_id
                        AND r.user_id = :user_id
                        AND r.deactivated_at IS NULL
                        AND r.deleted_at IS NULL
                    WHERE m.group_id = :group_id
                        AND m.user_id <> :user_id
                        AND m.joined_at IS NOT NULL
                        AND m.left_at IS NULL;
                    """,
                    "user_id" to userId,
                    "group_id" to groupId,
                )
            ) { row ->
                row.toGroupMember()
            }
        }

    fun insert(group: Group) = session {
        update(
                sql(
                    """
                    INSERT INTO groups (id, name, created_at)
                    VALUES (:id, :name, :created_at);
                    """,
                    *group.toParams(),
                )
            )
            .expectOne()
    }

    fun rename(id: UUID, name: String, userId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE groups
                    SET name = :name,
                        updated_by_user_id = :user_id,
                        updated_at = now()
                    WHERE id = :id
                    """,
                    "id" to id,
                    "name" to name,
                    "user_id" to userId,
                )
            )
            .expectOne()
    }

    fun join(membership: Membership) = session {
        update(
                sql(
                    """
                    INSERT INTO memberships (
                        user_id, group_id, share_relationships,
                        joined_at, left_at
                    )
                    VALUES (
                        :user_id, :group_id, :share_relationships,
                        :joined_at, :left_at
                    );
                    """,
                    *membership.toParams(),
                )
            )
            .expectOne()
    }

    fun leave(userId: UUID, groupId: UUID) = session {
        update(
                sql(
                    """
                    UPDATE memberships
                    SET left_at = now()
                    WHERE group_id = :group_id
                        AND user_id = :user_id;
                    """,
                    "user_id" to userId,
                    "group_id" to groupId,
                )
            )
            .expectOne()
    }
}
