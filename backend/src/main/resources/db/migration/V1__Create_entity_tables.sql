-- V1: Create the entity tables for the Fuckumeter domain model.
--
-- Conventions:
--   * uuid primary keys for exposed domain entities (users, relationships, groups, devices);
--   * bigint identity keys for internal events (memberships, entries, invites, recovery_requests);
--   * users and relationships are ordinarily soft-deleted (deactivated_at / deleted_at), but hard
--     deletes cascade so cleanup jobs can drop owned rows;
--   * database defaults don't enforce business rules: notification_enabled and notification_threshold
--     must be provided by application code.
--

CREATE DOMAIN level AS integer
    NOT NULL
    CONSTRAINT level_between_0_100 CHECK (VALUE BETWEEN 0 AND 100);

CREATE TYPE invite_type AS ENUM (
    'INVITE_USER',
    'JOIN_GROUP',
    'LINK_DEVICE',
    'RECOVERY'
    );

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    name            text        NOT NULL,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz,
    deactivated_at  timestamptz,
    deleted_at      timestamptz,

    CONSTRAINT users_update_after_create CHECK (updated_at >= created_at),
    CONSTRAINT users_deactivate_after_create CHECK (deactivated_at >= created_at),
    CONSTRAINT users_delete_requires_deactivate CHECK (deleted_at IS NULL OR deactivated_at IS NOT NULL),
    CONSTRAINT users_delete_after_deactivate CHECK (deleted_at >= deactivated_at)
);

-- ---------------------------------------------------------------------------
-- Relationships
-- ---------------------------------------------------------------------------
CREATE TABLE relationships (
    id                      uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    partner_id              uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    other_relationship_id   uuid        NOT NULL UNIQUE,
    nickname                text,
    notification_enabled    boolean     NOT NULL,
    notification_threshold  level,
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz,
    deactivated_at          timestamptz,
    deleted_at              timestamptz,

    -- Deferred so that the mutual references resolve at commit regardless of row order
    CONSTRAINT relationships_pair_fkey
        FOREIGN KEY (other_relationship_id) REFERENCES relationships (id)
        DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT relationships_no_self_relation CHECK (user_id <> partner_id),

    CONSTRAINT relationships_update_after_create CHECK (updated_at >= created_at),
    CONSTRAINT relationships_deactivate_after_create CHECK (deactivated_at >= created_at),
    -- Delete can cascade from partner deletion without deactivation
    CONSTRAINT relationships_delete_after_create CHECK (deleted_at >= created_at),
    CONSTRAINT relationships_delete_after_deactivate CHECK (deleted_at >= deactivated_at)
);

-- At most one active relationship per user-partner pair
CREATE UNIQUE INDEX relationships_user_partner_uq
    ON relationships (user_id, partner_id)
    WHERE deleted_at IS NULL;

CREATE INDEX relationships_user_id_idx ON relationships (user_id);

-- Useful for push notifications (get all partners)
CREATE INDEX relationships_partner_id_idx ON relationships (partner_id);

-- ---------------------------------------------------------------------------
-- Groups
-- ---------------------------------------------------------------------------
CREATE TABLE groups (
    id                  uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    name                text        NOT NULL,
    updated_by_user_id  uuid        REFERENCES users (id) ON DELETE SET NULL,
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz,

    CONSTRAINT groups_update_after_create CHECK (updated_at >= created_at)
);

-- ---------------------------------------------------------------------------
-- Memberships
-- ---------------------------------------------------------------------------
CREATE TABLE memberships (
    id                  bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    group_id            uuid        NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    share_relationships boolean     NOT NULL,
    joined_at           timestamptz,
    left_at             timestamptz,

    CONSTRAINT memberships_leave_after_join CHECK (left_at >= joined_at)
);

-- At most one active membership per user-group
CREATE UNIQUE INDEX memberships_user_group_uq
    ON memberships (user_id, group_id)
    WHERE left_at IS NULL;

-- Two-way access: list all groups for user, list all users for group
CREATE INDEX memberships_user_id_idx ON memberships (user_id);
CREATE INDEX memberships_group_id_idx ON memberships (group_id);

-- ---------------------------------------------------------------------------
-- Entries
-- ---------------------------------------------------------------------------
CREATE TABLE entries (
    id              bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    relationship_id uuid        NOT NULL REFERENCES relationships (id) ON DELETE CASCADE,
    level           level,
    created_at      timestamptz NOT NULL DEFAULT now()
);

-- For time-range queries
CREATE INDEX entries_relationship_id_created_at_idx
    ON entries (relationship_id, created_at);

-- ---------------------------------------------------------------------------
-- Devices
-- ---------------------------------------------------------------------------
CREATE TABLE devices (
    id                   uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name                 text        NOT NULL,
    notification_enabled boolean     NOT NULL,
    fcm_token            text        UNIQUE,
    refresh_token_hash   bytea       NOT NULL,
    created_at           timestamptz NOT NULL DEFAULT now(),
    last_seen_at         timestamptz,

    CONSTRAINT devices_see_after_create CHECK (last_seen_at >= created_at)
);

CREATE INDEX devices_user_id_idx ON devices (user_id);

-- ---------------------------------------------------------------------------
-- Invites
-- ---------------------------------------------------------------------------
CREATE TABLE invites (
    id                  bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created_by_user_id  uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    consumed_by_user_id uuid        REFERENCES users (id) ON DELETE SET NULL,
    code_hash           bytea       NOT NULL UNIQUE,
    code_ciphertext     bytea       NOT NULL,
    code_nonce          bytea       NOT NULL,
    type                invite_type NOT NULL,
    partner_id          uuid        REFERENCES users (id) ON DELETE CASCADE,
    group_id            uuid        REFERENCES groups (id) ON DELETE CASCADE,
    created_at          timestamptz NOT NULL DEFAULT now(),
    expires_at          timestamptz NOT NULL,
    consumed_at         timestamptz,
    revoked_at          timestamptz,

    CONSTRAINT invites_type_requires_columns
        CHECK (
            CASE type
                WHEN 'INVITE_USER'
                    THEN partner_id IS NULL
                    AND group_id IS NULL

                WHEN 'LINK_DEVICE'
                    THEN partner_id IS NULL
                    AND group_id IS NULL

                WHEN 'JOIN_GROUP'
                    THEN partner_id IS NULL
                    AND group_id IS NOT NULL

                WHEN 'RECOVERY'
                    THEN partner_id IS NOT NULL
                    AND group_id IS NULL
                END
            ),

    CONSTRAINT invites_consume_requires_by_user_and_time
        CHECK (
            (consumed_by_user_id IS NULL) = (consumed_at IS NULL)
            ),

    CONSTRAINT invites_expire_after_create CHECK (expires_at >= created_at),
    CONSTRAINT invites_consume_after_create CHECK (consumed_at >= created_at),
    CONSTRAINT invites_consume_before_expire CHECK (consumed_at < expires_at),
    CONSTRAINT invites_revoke_after_create CHECK (revoked_at >= created_at)
);

-- For cleanup jobs
CREATE INDEX invites_expires_at_idx ON invites (expires_at);

-- For latest (unfiltered) invites
CREATE INDEX invites_latest_idx
    ON invites (created_by_user_id, type, group_id, id DESC);

-- For the active invites view
CREATE INDEX invites_active_idx
    ON invites (expires_at)
    WHERE revoked_at IS NULL
        AND (consumed_at IS NULL OR type = 'JOIN_GROUP'::invite_type);

-- There is at most one active invite, older ones are revoked
CREATE UNIQUE INDEX invites_unrevoked_uq
    ON invites (created_by_user_id, type, group_id)
    WHERE revoked_at IS NULL;

CREATE VIEW active_invites AS
    SELECT *
    FROM invites
    WHERE (consumed_at IS NULL OR type = 'JOIN_GROUP'::invite_type)
      AND revoked_at IS NULL
      AND expires_at > now();

CREATE FUNCTION revoke_previous_invite()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE active_invites
    SET revoked_at = now()
    WHERE created_by_user_id = NEW.created_by_user_id
      AND type = NEW.type
      AND group_id IS NOT DISTINCT FROM NEW.group_id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER revoke_previous_invite
    BEFORE INSERT
    ON invites
    FOR EACH ROW
EXECUTE FUNCTION revoke_previous_invite();

-- ---------------------------------------------------------------------------
-- Recovery requests
-- ---------------------------------------------------------------------------
CREATE TABLE recovery_requests (
    id                    bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id               uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    partner_id            uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    invite_id             bigint      REFERENCES invites (id) ON DELETE CASCADE,
    created_at            timestamptz NOT NULL DEFAULT now(),
    revoked_at            timestamptz,
    revoked_by_partner_at timestamptz,

    CONSTRAINT recovery_requests_revoke_after_create CHECK (revoked_at >= created_at),
    CONSTRAINT recovery_requests_partner_revoke_after_create CHECK (revoked_by_partner_at >= created_at)
);

-- At most one open recovery request per user:
-- the user must revoke the request before submitting a new one
CREATE UNIQUE INDEX recovery_requests_user_unrevoked_uq
    ON recovery_requests (user_id)
    WHERE revoked_at IS NULL;

-- At most one open recovery request per target partner,
-- doesn't need active revocation by creating user
CREATE UNIQUE INDEX recovery_requests_partner_unrevoked_uq
    ON recovery_requests (partner_id)
    WHERE revoked_at IS NULL
      OR revoked_by_partner_at IS NULL;


