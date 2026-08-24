package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.service.*
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(
    deviceService: DeviceService,
    groupService: GroupService,
    inviteService: InviteService,
    relationshipService: RelationshipService,
    userService: UserService,
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        deviceRoutes(deviceService)
        groupRoutes(groupService)
        inviteRoutes(inviteService)
        relationshipRoutes(relationshipService)
        userRoutes(userService)
    }
}
