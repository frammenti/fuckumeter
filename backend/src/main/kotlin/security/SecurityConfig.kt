package dev.frammenti.fuckumeter.security

import io.ktor.server.config.ApplicationConfig
import java.util.Base64

class SecurityConfig(config: ApplicationConfig) {
    val refreshTokenSecret: ByteArray =
        Base64.getDecoder()
            .decode(config.property("security.refreshTokenSecret").getString())
    val inviteSecret: ByteArray =
        Base64.getDecoder()
            .decode(config.property("security.inviteSecret").getString())
    val inviteKey: ByteArray =
        Base64.getDecoder()
            .decode(config.property("security.inviteEncryptionKey").getString())

    constructor() : this(ApplicationConfig(null))
}
