package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.dto.InviteUserRequest
import dev.frammenti.fuckumeter.dto.JoinGroupRequest
import dev.frammenti.fuckumeter.dto.LinkDeviceRequest
import dev.frammenti.fuckumeter.dto.RecoveryRequest
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.security.UserDevicePrincipal
import dev.frammenti.fuckumeter.service.InviteService
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.inviteRoutes(service: InviteService) {
    authenticate(strategy = AuthenticationStrategy.Optional) {
        post("/invites/{code}/redeem") {
            // principal is null if no valid JWT was provided
            val principal = call.principal<UserDevicePrincipal>()

            // TODO: requireAnonymous for link device or profile recovery
        }
    }

    authenticate {
        post("/invites/user") {
            val request = call.receive<InviteUserRequest>()

            val response =
                service.inviteUser(
                    userId = call.userId,
                    groupId = request.groupId,
                )

            call.respond(response)
        }

        post("/invites/group") {
            val request = call.receive<JoinGroupRequest>()

            val response =
                service.joinGroup(
                    userId = call.userId,
                    groupId = request.groupId,
                )

            call.respond(response)
        }

        post("/invites/device") {
            val request = call.receive<LinkDeviceRequest>()

            val response =
                service.linkDevice(
                    userId = call.userId,
                    deviceName = request.deviceName,
                )

            call.respond(response)
        }

        // TODO: Integrate with recovery requests
        post("/invites/recovery") {
            val request = call.receive<RecoveryRequest>()

            val response =
                service.recovery(
                    userId = call.userId,
                    recoveryRequestId = request.recoveryRequestId,
                )

            call.respond(response)
        }
    }
}
