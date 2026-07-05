package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.service.InviteService
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import java.util.UUID

class InviteRoutes(private val inviteService: InviteService) {

    fun Routing.inviteRoutes() {

        get("/user/invite") {
            val response =
                inviteService.getInviteUser(
                    call.userId,
                    groupId = null,
                )

            call.respond(response)
        }

        get("/group/{id}/invite") {
            val groupId =
                UUID.fromString(call.parameters["id"]?.toString())
                    ?: throw IllegalArgumentException("Invalid Group ID")

            val response =
                inviteService.getJoinGroup(
                    call.userId,
                    groupId,
                )

            call.respond(response)
        }
    }
}
