package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class AuthenticationException(
    code: String,
    message: String = "Unauthorized",
) : ApiException(HttpStatusCode.Unauthorized, code, message)

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
        "authentication_required",
        "Authentication required",
    )
