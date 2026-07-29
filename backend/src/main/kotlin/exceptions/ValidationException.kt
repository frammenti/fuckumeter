package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class ValidationException(
    code: String,
    message: String = "Bad Request",
) : ApiException(HttpStatusCode.BadRequest, code, message)

class MissingParameterException(parameter: String) :
    ValidationException(
        "missing_parameter",
        "Missing required parameter \"$parameter\"",
    )

class InvalidParameterException(parameter: String, type: String) :
    ValidationException(
        "invalid_parameter",
        "Invalid parameter \"$parameter\", expected $type",
    )
