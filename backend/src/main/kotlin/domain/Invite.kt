package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.GROUP_JOIN_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.INVITE_CODE_LENGTH
import dev.frammenti.fuckumeter.domain.Defaults.INVITE_USER_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.LINK_DEVICE_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.RECOVERY_CODE_LENGTH
import dev.frammenti.fuckumeter.domain.Defaults.RECOVERY_EXPIRY
import kotliquery.Row
import java.security.SecureRandom
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

sealed class Invite(
    val createdBy: UUID,
    val consumedBy: UUID? = null,
    val createdAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant? = null,
    val revokedAt: Instant? = null,
) {
    abstract val type: InviteType

    lateinit var code: String
        protected set

    fun initializeCode(code: String) {
        check(!::code.isInitialized)
        this.code = code
    }

    constructor(
        row: Row
    ) : this(
        row.uuid("created_by_user_id"),
        row.uuidOrNull("consumed_by_user_id"),
        row.instant("created_at"),
        row.instant("expires_at"),
        row.instantOrNull("consumed_at"),
        row.instantOrNull("revoked_at"),
    )

    enum class InviteType {
        INVITE_USER,
        JOIN_GROUP,
        LINK_DEVICE,
        RECOVERY,
    }

    enum class InviteStatus {
        NONE,
        ACTIVE,
        EXPIRED,
        CONSUMED,
        REVOKED,
    }

    fun status(): InviteStatus {
        return when {
            this.consumedAt != null -> InviteStatus.CONSUMED
            this.revokedAt != null -> InviteStatus.REVOKED
            this.expiresAt >= now() -> InviteStatus.EXPIRED
            else -> InviteStatus.ACTIVE
        }
    }

    class InviteUser : Invite {
        override val type = InviteType.INVITE_USER
        val groupId: UUID?

        constructor(
            createdBy: UUID,
            code: String = Code.generate(),
            groupId: UUID? = null,
            createdAt: Instant = now(),
            expiresAt: Instant = createdAt.plus(INVITE_USER_EXPIRY),
        ) : super(
            createdBy = createdBy,
            createdAt = createdAt,
            expiresAt = expiresAt,
        ) {
            this.code = code
            this.groupId = groupId
        }

        constructor(row: Row) : super(row) {
            this.groupId = row.uuidOrNull("group_id")
        }
    }

    class JoinGroup : Invite {
        override val type = InviteType.JOIN_GROUP
        val groupId: UUID

        constructor(
            createdBy: UUID,
            code: String = Code.generate(),
            groupId: UUID,
            createdAt: Instant = now(),
            expiresAt: Instant = createdAt.plus(GROUP_JOIN_EXPIRY),
        ) : super(
            createdBy = createdBy,
            createdAt = createdAt,
            expiresAt = expiresAt,
        ) {
            this.code = code
            this.groupId = groupId
        }

        constructor(row: Row) : super(row) {
            groupId = row.uuid("group_id")
        }
    }

    class LinkDevice : Invite {
        override val type = InviteType.LINK_DEVICE
        val deviceName: String

        constructor(
            createdBy: UUID,
            code: String = Code.generate(),
            deviceName: String,
            createdAt: Instant = now(),
            expiresAt: Instant = createdAt.plus(LINK_DEVICE_EXPIRY),
        ) : super(
            createdBy = createdBy,
            createdAt = createdAt,
            expiresAt = expiresAt,
        ) {
            this.code = code
            this.deviceName = deviceName
        }

        constructor(row: Row) : super(row) {
            deviceName = row.string("device_name")
        }
    }

    class Recovery : Invite {
        override val type = InviteType.RECOVERY
        val recoveryRequestId: Int

        constructor(
            createdBy: UUID,
            code: String = Code.generate(RECOVERY_CODE_LENGTH),
            recoveryRequestId: Int,
            createdAt: Instant = now(),
            expiresAt: Instant = createdAt.plus(RECOVERY_EXPIRY),
        ) : super(
            createdBy = createdBy,
            createdAt = createdAt,
            expiresAt = expiresAt,
        ) {
            this.code = code
            this.recoveryRequestId = recoveryRequestId
        }

        constructor(row: Row) : super(row) {
            recoveryRequestId = row.int("recovery_request_id")
        }
    }

    private object Code {
        // Excludes 0/O, 1/I/L to avoid ambiguity when read aloud or handwritten
        private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        private val random = SecureRandom()

        fun generate(length: Int = INVITE_CODE_LENGTH): String {
            val sb = StringBuilder(length)
            repeat(length) {
                sb.append(ALPHABET[random.nextInt(ALPHABET.length)])
            }
            return sb.toString()
        }
    }

    companion object {
        infix fun factory(row: Row): Invite =
            when (InviteType.valueOf(row.string("type"))) {
                InviteType.INVITE_USER -> InviteUser(row)
                InviteType.JOIN_GROUP -> JoinGroup(row)
                InviteType.LINK_DEVICE -> LinkDevice(row)
                InviteType.RECOVERY -> Recovery(row)
            }
    }
}
