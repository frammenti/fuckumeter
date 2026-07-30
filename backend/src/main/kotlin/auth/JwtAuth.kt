package dev.frammenti.fuckumeter.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.frammenti.fuckumeter.exceptions.InvalidAccessTokenException
import dev.frammenti.fuckumeter.extensions.error
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt

fun Application.configureJwtAuth() {
    val config = JwtConfig(environment.config)

    authentication {
        jwt {
            realm = config.realm
            verifier(
                JWT.require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                try {
                    UserDevicePrincipal(credential.payload)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
            challenge { scheme, realm ->
                call.error(InvalidAccessTokenException(), realm)
            }
        }
    }
}
