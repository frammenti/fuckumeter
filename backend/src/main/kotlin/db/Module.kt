package dev.frammenti.fuckumeter.db

import io.ktor.server.application.Application

fun Application.initializeDatabase() {
    Database.initialize(this)
}
