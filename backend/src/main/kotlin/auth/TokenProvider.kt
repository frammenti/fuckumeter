package dev.frammenti.fuckumeter.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.frammenti.fuckumeter.shared.Time.now
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import java.util.UUID

class TokenProvider(private val config: JwtConfig) {
    private val random = SecureRandom()
    private val algorithm = Algorithm.HMAC256(config.secret)

    fun refreshToken(): String {
        val bytes = ByteArray(32) // 256 bits
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun accessToken(userId: UUID, deviceId: UUID): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withSubject(userId.toString())
            .withClaim("deviceId", deviceId.toString())
            .withExpiresAt(Date.from(now().plusSeconds(config.expiration)))
            .sign(algorithm)
    }
}
