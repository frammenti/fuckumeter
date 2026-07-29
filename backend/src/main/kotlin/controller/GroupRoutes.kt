package dev.frammenti.fuckumeter.controller

import dev.frammenti.fuckumeter.service.GroupService
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Routing

fun Routing.groupRoutes(service: GroupService) {
    authenticate {}
}
