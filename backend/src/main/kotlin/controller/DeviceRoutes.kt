package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.dto.RefreshTokenRequest
import dev.frammenti.fuckumeter.extensions.requireUUID
import dev.frammenti.fuckumeter.service.DeviceService
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.deviceRoutes(service: DeviceService) {
    // Unauthenticated
    post("/devices/{id}/refresh-token") {
        val deviceId = call.requireUUID("id")
        val request = call.receive<RefreshTokenRequest>()

        val response = service.refreshToken(deviceId, request.refreshToken)

        call.respond(response)
    }

    authenticate {}
}
