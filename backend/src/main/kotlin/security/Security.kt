package dev.frammenti.fuckumeter.security

import io.ktor.server.application.Application

fun Application.configureSecurity() {
    initCrypto()
    configureJwtAuth()
}
