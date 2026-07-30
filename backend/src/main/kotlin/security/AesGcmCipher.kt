package dev.frammenti.fuckumeter.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesGcmCipher(key: ByteArray) {

    companion object {
        private const val NONCE_LENGTH = 12
        private const val TAG_LENGTH = 128
    }

    private val secretKey = SecretKeySpec(key, "AES")
    private val random = SecureRandom()

    fun encrypt(plaintext: String): Encrypted {
        val nonce = ByteArray(NONCE_LENGTH)
        random.nextBytes(nonce)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey,
            GCMParameterSpec(TAG_LENGTH, nonce),
        )

        return Encrypted(
            ciphertext = cipher.doFinal(plaintext.toByteArray()),
            nonce = nonce,
        )
    }

    fun decrypt(encrypted: Encrypted): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(TAG_LENGTH, encrypted.nonce),
        )

        return cipher.doFinal(encrypted.ciphertext).decodeToString()
    }
}
