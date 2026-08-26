package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class NotFoundException(
    code: String = "not_found",
    message: String = "The requested resource does not exist",
) : ApiException(
    status = HttpStatusCode.NotFound,
    code = code,
    title = "Not Found",
    message = message,
)

class ResourceNotFoundException(resource: String = "resource") :
    NotFoundException(
        "${resource}_not_found",
        "${resource.replaceFirstChar { it.uppercase() }} does not exist",
    )

class InvalidCodeException() :
    NotFoundException(
        "invalid_invite_code",
        "Invite code is not valid",
    )
