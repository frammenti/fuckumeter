package dev.frammenti.fuckumeter.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.requireAnonymous() {
    check(principal<UserDevicePrincipal>() == null) {
        "Already authenticated."
    }
}

fun ApplicationCall.requireAuthenticated(): UserDevicePrincipal =
    checkNotNull(principal<UserDevicePrincipal>()) {
        "Authentication required."
    }

val ApplicationCall.userId: UUID
    get() {
        val principal =
            principal<UserDevicePrincipal>() ?: error("Principal not found")
        return principal.userId
    }

val ApplicationCall.deviceId: UUID
    get() {
        val principal =
            principal<UserDevicePrincipal>() ?: error("Principal not found")
        return principal.deviceId
    }
