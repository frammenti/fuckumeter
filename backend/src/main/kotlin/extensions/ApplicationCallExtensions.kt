package dev.frammenti.fuckumeter.extensions

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.dto.ConflictErrorResponse
import dev.frammenti.fuckumeter.dto.ErrorResponse
import dev.frammenti.fuckumeter.dto.LockedErrorResponse
import dev.frammenti.fuckumeter.exceptions.*
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.http.toHttpDateString
import io.ktor.server.response.respond
import java.util.UUID

fun ApplicationCall.requireParameter(parameter: String): String =
    parameters[parameter] ?: throw MissingParameterException(parameter)

fun ApplicationCall.requireUUID(parameter: String): UUID =
    requireParameter(parameter).let {
        try {
            UUID.fromString(it)
        } catch (_: IllegalArgumentException) {
            throw InvalidParameterException(parameter, "UUID")
        }
    }

fun ApplicationCall.optionalUUID(parameter: String): UUID? =
    parameters[parameter]?.let {
        try {
            UUID.fromString(it)
        } catch (_: IllegalArgumentException) {
            throw InvalidParameterException(parameter, "UUID")
        }
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

suspend fun ApplicationCall.error(cause: ApiException) =
    respond(
        status = cause.status,
        message = ErrorResponse(cause),
    )

// Unauthorized overload
suspend fun ApplicationCall.error(
    cause: AuthenticationException,
    realm: String,
) {
    val challenge =
        HttpAuthHeader.Parameterized(
            authScheme = "Bearer",
            parameters = mapOf(HttpAuthHeader.Parameters.Realm to realm),
        )
    response.headers.append(
        HttpHeaders.WWWAuthenticate,
        challenge.render(),
    )

    respond(
        status = cause.status,
        message = ErrorResponse(cause),
    )
}

// Conflict overload
suspend fun ApplicationCall.error(cause: ConflictException) =
    respond(
        status = cause.status,
        message = ConflictErrorResponse(cause),
    )

// Locked overload
suspend fun ApplicationCall.error(cause: LockedException) {
    response.headers.append(
        HttpHeaders.RetryAfter,
        cause.retryAfter.toHttpDateString(),
    )

    respond(LockedErrorResponse(cause))
}
