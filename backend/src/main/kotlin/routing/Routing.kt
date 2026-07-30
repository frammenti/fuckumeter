package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.service.DeviceService
import dev.frammenti.fuckumeter.service.GroupService
import dev.frammenti.fuckumeter.service.InviteService
import dev.frammenti.fuckumeter.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(
    deviceService: DeviceService,
    groupService: GroupService,
    inviteService: InviteService,
    userService: UserService,
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        deviceRoutes(deviceService)
        groupRoutes(groupService)
        inviteRoutes(inviteService)
        userRoutes(userService)
    }
}
