package dev.frammenti.fuckumeter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.frammenti.fuckumeter.dto.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureJwtAuth() {
    JwtConfig.initialize(environment.config)

    authentication {
        jwt {
            realm = JwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.secret))
                    .withAudience(JwtConfig.audience)
                    .withIssuer(JwtConfig.issuer)
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
                val challenge: HttpAuthHeader =
                    HttpAuthHeader.Parameterized(
                        authScheme = scheme,
                        parameters =
                            mapOf(HttpAuthHeader.Parameters.Realm to realm),
                    )

                call.response.headers.append(
                    HttpHeaders.WWWAuthenticate,
                    challenge.render(),
                )

                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        code = "invalid_access_token",
                        message = "Token is not valid or has expired",
                    ),
                )
            }
        }
    }
}
