package dev.frammenti.fuckumeter.domain

import dev.frammenti.fuckumeter.domain.Defaults.INVITE_CODE_LENGTH
import dev.frammenti.fuckumeter.domain.Defaults.INVITE_USER_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.JOIN_GROUP_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.LINK_DEVICE_EXPIRY
import dev.frammenti.fuckumeter.domain.Defaults.RECOVERY_CODE_LENGTH
import dev.frammenti.fuckumeter.domain.Defaults.RECOVERY_EXPIRY
import dev.frammenti.fuckumeter.shared.Time.now
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.UUID

sealed class Invite(protected open val lifecycle: Lifecycle) : InternalId {
    abstract val type: Type
    open val code = { Code.generate(INVITE_CODE_LENGTH) }

    val createdBy: UUID
        get() = lifecycle.createdBy

    val consumedBy: UUID?
        get() = lifecycle.consumedBy

    val createdAt: Instant
        get() = lifecycle.createdAt

    val expiresAt: Instant
        get() = lifecycle.expiresAt

    val consumedAt: Instant?
        get() = lifecycle.consumedAt

    val revokedAt: Instant?
        get() = lifecycle.revokedAt

    data class Lifecycle(
        val createdBy: UUID,
        val consumedBy: UUID? = null,
        val createdAt: Instant,
        val expiresAt: Instant,
        val consumedAt: Instant? = null,
        val revokedAt: Instant? = null,
    ) {
        constructor(
            createdBy: UUID,
            createdAt: Instant = now(),
            expiry: TemporalAmount,
        ) : this(
            createdBy = createdBy,
            createdAt = createdAt,
            expiresAt = createdAt.plus(expiry),
        )
    }

    enum class Type {
        INVITE_USER,
        JOIN_GROUP,
        LINK_DEVICE,
        RECOVERY,
    }

    enum class Status {
        NONE,
        ACTIVE,
        EXPIRED,
        CONSUMED,
        REVOKED,
    }

    enum class RedemptionStatus {
        COMPLETED,
        REQUIRES_USER,
        REQUIRES_DEVICE,
    }

    fun status(): Status {
        return when {
            this.consumedAt != null && this.type != Type.JOIN_GROUP ->
                Status.CONSUMED
            this.revokedAt != null -> Status.REVOKED
            this.expiresAt <= now() -> Status.EXPIRED
            else -> Status.ACTIVE
        }
    }

    data class WithCode<out I : Invite>(
        val invite: I,
        val code: String = invite.code(),
    )

    typealias InviteWithCode = WithCode<Invite>

    data class InviteUser(override val lifecycle: Lifecycle) :
        Invite(lifecycle) {
        override val type = Companion.type

        constructor(
            createdBy: UUID
        ) : this(Lifecycle(createdBy = createdBy, expiry = expiry))

        companion object {
            val type = Type.INVITE_USER
            val expiry = INVITE_USER_EXPIRY
        }
    }

    data class JoinGroup(
        val groupId: UUID,
        override val lifecycle: Lifecycle,
    ) : Invite(lifecycle) {
        override val type = Companion.type

        constructor(
            createdBy: UUID,
            groupId: UUID,
        ) : this(groupId, Lifecycle(createdBy = createdBy, expiry = expiry))

        companion object {
            val type = Type.JOIN_GROUP
            val expiry = JOIN_GROUP_EXPIRY
        }
    }

    data class LinkDevice(override val lifecycle: Lifecycle) :
        Invite(lifecycle) {
        override val type = Companion.type

        constructor(
            createdBy: UUID
        ) : this(Lifecycle(createdBy = createdBy, expiry = expiry))

        companion object {
            val type = Type.LINK_DEVICE
            val expiry = LINK_DEVICE_EXPIRY
        }
    }

    data class Recovery(
        val relationshipId: UUID,
        override val lifecycle: Lifecycle,
    ) : Invite(lifecycle) {
        override val type = Companion.type
        override val code = { Code.generate(RECOVERY_CODE_LENGTH) }

        constructor(
            createdBy: UUID,
            relationshipId: UUID,
        ) : this(
            relationshipId,
            Lifecycle(createdBy = createdBy, expiry = expiry),
        )

        companion object {
            val type = Type.RECOVERY
            val expiry = RECOVERY_EXPIRY
        }
    }

    private object Code {
        // Excludes 0/O, 1/I/L to avoid ambiguity when copied manually
        private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        private val random = SecureRandom()

        fun generate(length: Int): String {
            val sb = StringBuilder(length)
            repeat(length) {
                sb.append(ALPHABET[random.nextInt(ALPHABET.length)])
            }
            return sb.toString()
        }
    }
}
