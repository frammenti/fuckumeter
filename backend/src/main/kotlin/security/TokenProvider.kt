package dev.frammenti.fuckumeter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.frammenti.fuckumeter.dto.TokenPair
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID

class TokenProvider {
    private val random = SecureRandom()
    private val algorithm = Algorithm.HMAC256(JwtConfig.secret)

    fun refreshToken(): String {
        val bytes = ByteArray(32) // 256 bits
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun accessToken(userId: UUID, deviceId: UUID): String {
        return JWT.create()
            .withAudience(JwtConfig.audience)
            .withIssuer(JwtConfig.issuer)
            .withSubject(userId.toString())
            .withClaim("deviceId", deviceId.toString())
            .withExpiresAt(
                Date.from(Instant.now().plusSeconds(JwtConfig.expiration))
            )
            .sign(algorithm)
    }

    fun pair(userId: UUID, deviceId: UUID): TokenPair {
        return TokenPair(accessToken(userId, deviceId), refreshToken())
    }
}
