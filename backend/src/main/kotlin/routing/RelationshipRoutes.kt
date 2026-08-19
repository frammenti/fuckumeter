package dev.frammenti.fuckumeter.routing

import dev.frammenti.fuckumeter.dto.RelationshipRequest
import dev.frammenti.fuckumeter.extensions.userId
import dev.frammenti.fuckumeter.service.RelationshipService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post

fun Routing.relationshipRoutes(service: RelationshipService) {
    authenticate {
        post("/relationships") {
            val request = call.receive<RelationshipRequest>()
            val response = service.createPair(call.userId to request.partnerId)

            call.respond(HttpStatusCode.Created, response)
        }
    }

    authenticate {}
}
