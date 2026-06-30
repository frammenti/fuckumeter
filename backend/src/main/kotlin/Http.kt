package dev.frammenti.fuckumeter

import io.ktor.server.application.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHttp() {
    install(HSTS) {
        includeSubDomains = true
    }
}
