package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class ValidationException(
    code: String,
    message: String = "Bad Request",
) : ApiException(HttpStatusCode.BadRequest, code, message)

class MissingParameterException(parameter: String) :
    ValidationException(
        "missing_parameter",
        "Required parameter \"$parameter\" is missing",
    )

class InvalidParameterException(parameter: String, type: String) :
    ValidationException(
        "invalid_parameter",
        "Parameter \"$parameter\" is not valid, expected $type",
    )

class MissingRequestBodyException(message: String?) :
    ValidationException(
        "missing_request_body",
        message ?: "Request body is missing",
    )

class InvalidRequestBodyException(message: String?) :
    ValidationException(
        "invalid_request_body",
        message ?: "Request body is not valid",
    )
