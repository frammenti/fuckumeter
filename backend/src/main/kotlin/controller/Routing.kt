package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.repository.DeviceRepository
import dev.frammenti.fuckumeter.repository.GroupRepository
import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.repository.UserRepository
import dev.frammenti.fuckumeter.security.TokenProvider
import dev.frammenti.fuckumeter.service.DeviceService
import dev.frammenti.fuckumeter.service.GroupService
import dev.frammenti.fuckumeter.service.InviteService
import dev.frammenti.fuckumeter.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val tokenProvider = TokenProvider()
    val deviceRepository = DeviceRepository()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        deviceRoutes(DeviceService(deviceRepository, tokenProvider))
        groupRoutes(GroupService(GroupRepository()))
        inviteRoutes(InviteService(InviteRepository()))
        userRoutes(
            UserService(
                UserRepository(),
                deviceRepository,
                tokenProvider,
            )
        )
    }
}
