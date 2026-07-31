package fixtures

import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.HmacHasher
import java.util.Base64

object TestCrypto {
    private val secret: ByteArray =
        Base64.getDecoder()
            .decode("FUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU=")
    val hasher = HmacHasher(secret)
    val cipher = AesGcmCipher(secret)
}
