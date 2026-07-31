package dev.frammenti.fuckumeter.db

import io.ktor.server.config.ApplicationConfig

class DatabaseConfig(config: ApplicationConfig) {
    val url = config.property("database.url").getString()
    val user = config.property("database.user").getString()
    val password = config.property("database.password").getString()

    constructor() : this(ApplicationConfig(null))
}
