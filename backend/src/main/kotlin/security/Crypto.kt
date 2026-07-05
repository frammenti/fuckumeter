package dev.frammenti.fuckumeter.security

import io.ktor.server.application.Application
import java.util.HexFormat

private var RefreshHasherInit: HmacHasher? = null
private var InviteHasherInit: HmacHasher? = null
private var InviteCipherInit: AesGcmCipher? = null

val RefreshHasher: HmacHasher
    get() =
        RefreshHasherInit
            ?: error("RefreshHasher accessed before configureSecurity() ran")

val InviteHasher: HmacHasher
    get() =
        InviteHasherInit
            ?: error("InviteHasher accessed before configureSecurity() ran")

val InviteCipher: AesGcmCipher
    get() =
        InviteCipherInit
            ?: error("InviteCipher accessed before configureSecurity() ran")

fun Application.initCrypto() {
    val config = environment.config
    val refreshTokenSecret = config.property("refreshTokenSecret").getString()
    val inviteSecret = config.property("inviteSecret").getString()
    val inviteKey =
        HexFormat.of()
            .parseHex(config.property("inviteEncryptionKey").getString())

    InviteCipherInit = AesGcmCipher(inviteKey)

    RefreshHasherInit = HmacHasher(refreshTokenSecret)
    InviteHasherInit = HmacHasher(inviteSecret)
    InviteCipherInit = AesGcmCipher(inviteKey)
}
