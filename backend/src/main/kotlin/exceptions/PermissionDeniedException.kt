package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

class PermissionDeniedException(
    message: String = "You are not allowed to perform this operation",
) : ApiException(HttpStatusCode.Forbidden, "permission_denied", message)
