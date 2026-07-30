package dev.frammenti.fuckumeter.auth

import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.jwt.JWTPayloadHolder
import java.util.UUID

class UserDevicePrincipal(payload: Payload) : JWTPayloadHolder(payload) {

    private fun String.UUID(name: String): UUID =
        try {
            UUID.fromString(this)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("$name is not a valid UUID")
        }

    val userId: UUID =
        payload.subject?.takeIf(String::isNotBlank)?.UUID("subject")
            ?: throw IllegalArgumentException("Missing subject")

    val deviceId: UUID =
        payload
            .getClaim("deviceId")
            .asString()
            ?.takeIf(String::isNotBlank)
            ?.UUID("deviceId")
            ?: throw IllegalArgumentException("Missing deviceId")
}
