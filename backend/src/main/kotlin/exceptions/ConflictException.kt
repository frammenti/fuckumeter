package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class ConflictException(
    code: String = "conflict",
    message: String = "Conflict",
) : ApiException(HttpStatusCode.Conflict, code, message)

class AlreadyAuthenticatedException :
    ConflictException(
        "already_authenticated",
        "This device is already associated with a user",
    )
