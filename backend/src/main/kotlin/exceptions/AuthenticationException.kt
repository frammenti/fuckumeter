package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class AuthenticationException(
    code: String = "unauthorized",
    message: String = "You are not authorized to access this resource",
) : ApiException(
    status = HttpStatusCode.Unauthorized,
    code = code,
    title = "Authentication Error",
    message = message,
)

class InvalidAccessTokenException :
    AuthenticationException(
        "invalid_access_token",
        "Access token is not valid or has expired",
    )

class InvalidRefreshTokenException :
    AuthenticationException(
        "invalid_refresh_token",
        "Refresh token is not valid",
    )
