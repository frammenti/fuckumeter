package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class AuthenticationException(
    code: String,
    message: String = "Unauthorized",
) : ApiException(HttpStatusCode.Unauthorized, code, message)

class InvalidAccessTokenException :
    AuthenticationException(
        "invalid_access_token",
        "Token is not valid or has expired",
    )

class InvalidRefreshTokenException :
    AuthenticationException(
        "invalid_refresh_token",
        "Refresh token is not valid",
    )
