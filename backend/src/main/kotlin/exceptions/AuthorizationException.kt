package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class AuthorizationException(
    code: String,
    message: String = "Forbidden",
) : ApiException(HttpStatusCode.Forbidden, code, message)
