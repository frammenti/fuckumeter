package dev.frammenti.fuckumeter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.frammenti.fuckumeter.dto.UsersResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.Date

fun validate(credential: JWTCredential): UserDevicePrincipal? =
    try {
        UserDevicePrincipal(credential.payload)
    } catch (_: IllegalArgumentException) {
        null
    }

fun Application.configureJwtAuth() {
    val config = environment.config

    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val realm = environment.config.property("jwt.realm").getString()
    val secret = config.property("jwt.secret").getString()

    authentication {
        jwt {
            secret
            issuer
            audience
            realm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            ::validate
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "Token is not valid or has expired",
                )
            }
        }
    }

    routing {
        authenticate(strategy = AuthenticationStrategy.Optional) {
            post("/users") {
                call.requireAnonymous()

                val token =
                    JWT.create()
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .withExpiresAt(
                            Date(System.currentTimeMillis() + 60_000)
                        )
                        .sign(Algorithm.HMAC256(secret))

                call.respond(UsersResponse(token))
            }

            post("/invites/{code}/redeem") {
                val principal = call.principal<UserDevicePrincipal>()

                // principal is null if no valid JWT was provided
            }
        }
    }
}
