package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.dto.UsersRequest
import dev.frammenti.fuckumeter.extensions.requireAnonymous
import dev.frammenti.fuckumeter.service.UserService
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.userRoutes(service: UserService) {
    authenticate(strategy = AuthenticationStrategy.Optional) {
        post("/users") {
            call.requireAnonymous()

            val request = call.receive<UsersRequest>()
            val response = service.new(request.name, request.deviceName)

            call.respond(response)
        }
    }

    authenticate {}
}
