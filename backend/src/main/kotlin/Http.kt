package dev.frammenti.fuckumeter

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.hsts.HSTS

fun Application.configureHttp() {
    install(HSTS) {
        includeSubDomains = true
    }
}
