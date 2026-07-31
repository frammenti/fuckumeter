package dev.frammenti.fuckumeter.auth

import io.ktor.server.config.ApplicationConfig

class JwtConfig(config: ApplicationConfig) {
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val realm = config.property("jwt.realm").getString()
    val secret = config.property("jwt.secret").getString()
    val expiration =
        config.property("jwt.accessTokenExpirationSeconds").getString().toLong()

    constructor() : this(ApplicationConfig(null))
}
