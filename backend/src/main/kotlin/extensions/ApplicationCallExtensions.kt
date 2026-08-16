package dev.frammenti.fuckumeter.extensions

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.dto.ErrorResponse
import dev.frammenti.fuckumeter.exceptions.*
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import java.util.UUID

fun ApplicationCall.requireParameter(parameter: String): String =
    parameters[parameter] ?: throw MissingParameterException(parameter)

fun ApplicationCall.requireUUID(parameter: String): UUID =
    try {
        UUID.fromString(requireParameter(parameter))
    } catch (_: IllegalArgumentException) {
        throw InvalidParameterException(parameter, "UUID")
    }

fun ApplicationCall.requireAnonymous() {
    if (principal<UserDevicePrincipal>() != null) {
        throw AlreadyAuthenticatedException()
    }
}

fun ApplicationCall.requireAuthenticated(): UserDevicePrincipal =
    principal<UserDevicePrincipal>() ?: throw InvalidAccessTokenException()

val ApplicationCall.userId: UUID
    get() = requireAuthenticated().userId

val ApplicationCall.deviceId: UUID
    get() = requireAuthenticated().deviceId

suspend fun ApplicationCall.error(cause: ApiException, realm: String? = null) {
    if (cause is AuthenticationException && realm != null) {
        val challenge =
            HttpAuthHeader.Parameterized(
                authScheme = "Bearer",
                parameters = mapOf(HttpAuthHeader.Parameters.Realm to realm),
            )
        response.headers.append(
            HttpHeaders.WWWAuthenticate,
            challenge.render(),
        )
    }

    respond(
        status = cause.status,
        message = ErrorResponse(cause.code, cause.message),
    )
}
