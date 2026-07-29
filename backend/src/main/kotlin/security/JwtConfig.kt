package dev.frammenti.fuckumeter.security

import io.ktor.server.config.ApplicationConfig

object JwtConfig {
    lateinit var issuer: String
        private set

    lateinit var audience: String
        private set

    lateinit var realm: String
        private set

    lateinit var secret: String
        private set

    var expiration: Long = 0
        private set

    fun initialize(config: ApplicationConfig) {
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        realm = config.property("jwt.realm").getString()
        secret = config.property("jwt.secret").getString()
        expiration =
            config
                .property("jwt.accessTokenExpirationSeconds")
                .getString()
                .toLong()
    }
}
