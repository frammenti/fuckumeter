package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.dto.RefreshTokenRequest
import dev.frammenti.fuckumeter.extensions.deviceId
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.service.DeviceService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.authRoutes(service: DeviceService) {
    // Unauthenticated
    post("/auth/refresh") {
        val (deviceId, refreshToken) = call.receive<RefreshTokenRequest>()

        val response = service.refreshToken(deviceId, refreshToken)

        call.respond(response)
    }

    authenticate {
        post("auth/logout") {
            service.logout(call.deviceId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
