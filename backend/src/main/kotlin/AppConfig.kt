package dev.frammenti.fuckumeter

import dev.frammenti.fuckumeter.auth.JwtConfig
import dev.frammenti.fuckumeter.db.DatabaseConfig
import dev.frammenti.fuckumeter.security.SecurityConfig
import io.ktor.server.config.ApplicationConfig

class AppConfig(config: ApplicationConfig) {
    val database = DatabaseConfig(config)
    val jwt = JwtConfig(config)
    val security = SecurityConfig(config)
}
