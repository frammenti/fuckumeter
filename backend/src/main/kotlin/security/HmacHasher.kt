package dev.frammenti.fuckumeter.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacHasher(secret: ByteArray) {
    private val key =
        SecretKeySpec(
            secret,
            "HmacSHA256",
        )

    fun hash(value: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(key)
        return mac.doFinal(value.toByteArray())
    }
}
