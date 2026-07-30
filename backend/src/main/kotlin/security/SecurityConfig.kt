package dev.frammenti.fuckumeter.security

import io.ktor.server.config.ApplicationConfig
import java.util.HexFormat

class SecurityConfig(config: ApplicationConfig) {
    val refreshTokenSecret =
        config.property("security.refreshTokenSecret").getString()
    val inviteSecret = config.property("security.inviteSecret").getString()
    val inviteKey: ByteArray =
        HexFormat.of()
            .parseHex(
                config.property("security.inviteEncryptionKey").getString()
            )
}
