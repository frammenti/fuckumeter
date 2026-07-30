package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

class NotFoundException(message: String = "Not found") :
    ApiException(HttpStatusCode.NotFound, "not_found", message)
