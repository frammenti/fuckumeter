package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

class UnhandledException(message: String = "An unexpected error occurred") :
    ApiException(
        status = HttpStatusCode.InternalServerError,
        code = "server_error",
        title = "Internal Server Error",
        message = message,
    )
