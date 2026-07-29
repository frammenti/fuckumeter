package dev.frammenti.fuckumeter.exceptions

import dev.frammenti.fuckumeter.security.JwtConfig.realm
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader

sealed class AuthenticationException(
    code: String,
    message: String = "Unauthorized",
) : ApiException(HttpStatusCode.Unauthorized, code, message) {
    val challenge: HttpAuthHeader =
        HttpAuthHeader.Parameterized(
            authScheme = "Bearer",
            parameters = mapOf(HttpAuthHeader.Parameters.Realm to realm),
        )
}

class ExpiredAccessTokenException :
    AuthenticationException(
        "expired_access_token",
        "Expired access token",
    )

class InvalidRefreshTokenException :
    AuthenticationException(
        "invalid_refresh_token",
        "Invalid refresh token",
    )

class AuthenticationRequiredException :
    AuthenticationException(
        code = "authentication_required",
        message = "Authentication required",
    )
