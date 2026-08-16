package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class NotFoundException(
    code: String = "not_found",
    message: String = "Not found",
) : ApiException(HttpStatusCode.NotFound, code, message)

class ResourceNotFoundException(resource: String = "resource") :
    NotFoundException(
        "${resource}_not_found",
        "${resource.replaceFirstChar { it.uppercase() }} does not exist",
    )

class InvalidCodeException() :
    NotFoundException(
        "invalid_code",
        "Code is not valid",
    )
