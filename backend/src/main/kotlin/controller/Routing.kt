package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.repository.InviteRepository
import dev.frammenti.fuckumeter.service.InviteService
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }

    val inviteService = InviteService(InviteRepository())

    routing {
        authenticate {
            InviteRoutes(inviteService)
        }
    }
}
