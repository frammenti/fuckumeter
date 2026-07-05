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
    CHECK (VALUE BETWEEN 0 AND 100);

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
    deleted_at      timestamptz
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
    CONSTRAINT relationships_no_self_relation CHECK (user_id <> partner_id)
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
    updated_at          timestamptz
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
    left_at             timestamptz
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
    last_accessed_at     timestamptz
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
    type                invite_type NOT NULL
                                    CHECK (
                                        CASE type
                                            WHEN 'INVITE_USER'
                                                THEN device_name IS NULL
                                                AND recovery_request_id IS NULL

                                            WHEN 'JOIN_GROUP'
                                                THEN group_id IS NOT NULL
                                                AND device_name IS NULL
                                                AND recovery_request_id IS NULL

                                            WHEN 'LINK_DEVICE'
                                                THEN group_id IS NULL
                                                AND device_name IS NOT NULL
                                                AND recovery_request_id IS NULL

                                            WHEN 'RECOVERY'
                                                THEN group_id IS NULL
                                                AND device_name IS NULL
                                                AND recovery_request_id IS NOT NULL
                                            END
                                    ),
    group_id            uuid,
    device_name         text,
    recovery_request_id bigint,
    created_at          timestamptz NOT NULL DEFAULT now(),
    expires_at          timestamptz NOT NULL,
    consumed_at         timestamptz,
    revoked_at          timestamptz
);

CREATE INDEX invites_created_by_user_id_idx ON invites (created_by_user_id);

-- Single invite per group
CREATE INDEX invites_group_id_idx ON invites (group_id);

-- For cleanup jobs
CREATE INDEX invites_expires_at_idx ON invites (expires_at);

-- ---------------------------------------------------------------------------
-- Recovery requests
-- ---------------------------------------------------------------------------
CREATE TABLE recovery_requests (
    id              bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    relationship_id uuid        NOT NULL REFERENCES relationships (id) ON DELETE CASCADE,
    revoked_by_id   uuid        REFERENCES users (id) ON DELETE SET NULL,
    created_at      timestamptz NOT NULL DEFAULT now(),
    consumed_at     timestamptz,
    revoked_at      timestamptz
);

-- At most one open recovery request per relationship
CREATE UNIQUE INDEX recovery_requests_open_uq
    ON recovery_requests (relationship_id)
    WHERE consumed_at IS NULL AND revoked_at IS NULL;

CREATE INDEX recovery_requests_relationship_id_idx
    ON recovery_requests (relationship_id);
