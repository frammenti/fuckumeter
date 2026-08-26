package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode
import java.util.UUID

sealed class ConflictException(
    code: String = "conflict",
    message: String =
        "The request could not be processed because it conflicts with the resource state",
    val causeId: UUID? = null,
) :
    ApiException(
        status = HttpStatusCode.Conflict,
        code = code,
        title = "Conflict Error",
        message = message,
    )

class AlreadyAuthenticatedException :
    ConflictException(
        "already_authenticated",
        "This device is already associated with a user",
    )

class ConcurrentUpdateException(
    resource: String = "resource",
    operation: String = "modified",
) :
    ConflictException(
        "concurrent_update",
        "The $resource was already $operation",
    )

class AnotherRecoveryInviteException(causeId: UUID) :
    ConflictException(
        "another_recovery_invite",
        "There is pending recovery invite for another relationship, revoke it to proceed",
        causeId,
    )
