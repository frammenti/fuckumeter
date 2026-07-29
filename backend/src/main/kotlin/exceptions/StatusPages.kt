package dev.frammenti.fuckumeter.exceptions

import dev.frammenti.fuckumeter.dto.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Custom exceptions
        exception<ApiException> { call, cause ->
            if (cause is AuthenticationException) {
                call.response.headers.append(
                    HttpHeaders.WWWAuthenticate,
                    cause.challenge.render(),
                )
            }

            call.respond(
                cause.status,
                ErrorResponse(
                    code = cause.code,
                    message = cause.message,
                ),
            )
        }

        // Ktor validation exceptions
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "invalid_request_body",
                    message = cause.message ?: "Invalid request body",
                ),
            )
        }

        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "missing_request_body",
                    message = cause.message ?: "Missing request body",
                ),
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    code = "not_found",
                    message = cause.message ?: "Not found",
                ),
            )
        }

        // Catchall
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = "server_error",
                    message = "Internal server error",
                ),
            )
        }
    }
}
