package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

class UnhandledException(message: String = "Internal server error") :
    ApiException(HttpStatusCode.InternalServerError, "server_error", message)
