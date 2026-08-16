package dev.frammenti.fuckumeter.domain

import java.time.Duration

object Defaults {
    const val NOTIFICATION_ENABLED_RELATIONSHIP = true
    const val NOTIFICATION_ENABLED_DEVICE = true
    const val NOTIFICATION_THRESHOLD = 80

    const val RELATIONSHIP_LEVEL = 0

    const val USER_NAME = "stranger"

    const val SHARE_RELATIONSHIPS = false

    const val INVITE_CODE_LENGTH = 8
    const val RECOVERY_CODE_LENGTH = 12

    val INVITE_USER_EXPIRY: Duration = Duration.ofDays(7)
    val JOIN_GROUP_EXPIRY: Duration = Duration.ofDays(30)
    val LINK_DEVICE_EXPIRY: Duration = Duration.ofMinutes(15)
    val RECOVERY_EXPIRY: Duration = Duration.ofHours(24)
    val RECOVERY_WAIT: Duration = Duration.ofHours(24)
}
