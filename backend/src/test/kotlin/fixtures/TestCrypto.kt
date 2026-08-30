package fixtures

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.dto.UUID
import dev.frammenti.fuckumeter.security.AesGcmCipher
import dev.frammenti.fuckumeter.security.HmacHasher
import java.util.Base64

object TestCrypto {
    private val secret: ByteArray =
        Base64.getDecoder()
            .decode("FUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU=")
    private val baseInvite = Invite.InviteUser(UUID.randomUUID())
    val hasher = HmacHasher(secret)
    val cipher = AesGcmCipher(secret)
    val code = baseInvite.code
}
