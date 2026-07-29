package dev.frammenti.fuckumeter.extensions

import dev.frammenti.fuckumeter.exceptions.AlreadyAuthenticatedException
import dev.frammenti.fuckumeter.exceptions.AuthenticationRequiredException
import dev.frammenti.fuckumeter.exceptions.InvalidParameterException
import dev.frammenti.fuckumeter.exceptions.MissingParameterException
import dev.frammenti.fuckumeter.security.UserDevicePrincipal
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.requireUUID(parameter: String): UUID =
    try {
        UUID.fromString(
            parameters[parameter] ?: throw MissingParameterException(parameter)
        )
    } catch (_: IllegalArgumentException) {
        throw InvalidParameterException(parameter, "UUID")
    }

fun ApplicationCall.requireAnonymous() {
    if (principal<UserDevicePrincipal>() != null) {
        throw AlreadyAuthenticatedException()
    }
}

fun ApplicationCall.requireAuthenticated(): UserDevicePrincipal =
    principal<UserDevicePrincipal>() ?: throw AuthenticationRequiredException()

val ApplicationCall.userId: UUID
    get() = requireAuthenticated().userId

val ApplicationCall.deviceId: UUID
    get() = requireAuthenticated().deviceId
