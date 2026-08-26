package dev.frammenti.fuckumeter.exceptions

import dev.frammenti.fuckumeter.auth.JwtConfig
import dev.frammenti.fuckumeter.extensions.error
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages

fun Application.configureStatusPages() {
    val realm = JwtConfig(environment.config).realm

    install(StatusPages) {
        // Auth exceptions
        exception<AuthenticationException> { call, cause ->
            call.error(cause, realm)
        }

        // Conflict exceptions
        exception<ConflictException> { call, cause ->
            call.error(cause)
        }

        // Locked exceptions
        exception<LockedException> { call, cause ->
            call.error(cause)
        }

        // Other custom exceptions
        exception<ApiException> { call, cause ->
            call.error(cause)
        }

        // Ktor validation exceptions
        exception<BadRequestException> { call, cause ->
            call.error(InvalidRequestBodyException(cause.message))
        }

        exception<ContentTransformationException> { call, cause ->
            call.error(MissingRequestBodyException(cause.message))
        }

        // Catchall
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.error(UnhandledException())
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.error(ResourceNotFoundException())
        }
    }
}
