package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.extensions.requireUUID
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.service.DeviceService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete

fun Routing.deviceRoutes(service: DeviceService) {
    authenticate {
        delete("devices/{id}") {
            val deviceId = call.requireUUID("id")
            service.delete(deviceId, call.userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
