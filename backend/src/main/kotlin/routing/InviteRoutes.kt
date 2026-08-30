package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.auth.UserDevicePrincipal
import dev.frammenti.fuckumeter.domain.Invite.*
import dev.frammenti.fuckumeter.dto.RedemptionRequest
import dev.frammenti.fuckumeter.extensions.optionalUUID
import dev.frammenti.fuckumeter.extensions.requireParameter
import dev.frammenti.fuckumeter.extensions.requireUUID
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.service.InviteService
import dev.frammenti.fuckumeter.service.RedemptionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.post

fun Routing.inviteRoutes(
    service: InviteService,
    redemptionService: RedemptionService,
) {
    authenticate(strategy = AuthenticationStrategy.Optional) {
        post("/invites/{code}/redeem") {
            val code = call.requireParameter("code")
            val (username, deviceName) =
                call.receiveNullable<RedemptionRequest>() ?: RedemptionRequest()

            // principal is null if no valid JWT was provided
            val principal = call.principal<UserDevicePrincipal>()

            val response =
                redemptionService.redeem(
                    code,
                    principal?.userId,
                    username,
                    deviceName,
                )

            call.respond(response)
        }
    }

    authenticate {
        post("/invites/user") {
            val response = service.inviteUser(userId = call.userId)

            call.respond(response)
        }

        post("/invites/group/{groupId}") {
            val groupId = call.requireUUID("groupId")

            val response =
                service.joinGroup(
                    userId = call.userId,
                    groupId = groupId,
                )

            call.respond(response)
        }

        post("/invites/device") {
            val response = service.linkDevice(userId = call.userId)

            call.respond(response)
        }

        post("/invites/recovery/{partnerId?}") {
            val partnerId = call.optionalUUID("partnerId")

            val response =
                service.recovery(
                    userId = call.userId,
                    partnerId = partnerId,
                )

            call.respond(response ?: HttpStatusCode.Created)
        }
    }

    delete("/invites/user") {
        service.revoke(call.userId, InviteUser.type)

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/invites/group/{groupId}") {
        val groupId = call.requireUUID("groupId")

        service.revoke(call.userId, JoinGroup.type, groupId)

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/invites/device") {
        service.revoke(call.userId, LinkDevice.type)

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/invites/recovery") {
        service.revokeRecoveryByUser(call.userId)

        call.respond(HttpStatusCode.NoContent)
    }

    delete("/invites/recovery-request") {
        service.revokeRecoveryByPartner(call.userId)

        call.respond(HttpStatusCode.NoContent)
    }
}
