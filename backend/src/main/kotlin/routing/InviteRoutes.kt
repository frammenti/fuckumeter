package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.dto.InviteRequest.JoinGroupRequest
import dev.frammenti.fuckumeter.dto.InviteRequest.RecoveryRequest
import dev.frammenti.fuckumeter.dto.RedemptionRequest
import dev.frammenti.fuckumeter.extensions.requireParameter
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.service.InviteService
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.inviteRoutes(service: InviteService) {
    authenticate(strategy = AuthenticationStrategy.Optional) {
        post("/invites/{code}/redeem") {
            val code = call.requireParameter("code")
            val (username, deviceName) =
                call.receiveNullable<RedemptionRequest>() ?: RedemptionRequest()

            // principal is null if no valid JWT was provided
            val principal = call.principal<UserDevicePrincipal>()

            val response = service.redeem(code, principal, username, deviceName)
            call.respond(response)
        }
    }

    authenticate {
        post("/invites/user") {
            val response = service.inviteUser(userId = call.userId)

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
            val response = service.linkDevice(userId = call.userId)

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
