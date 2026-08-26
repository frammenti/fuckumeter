package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode
import java.time.Instant

sealed class LockedException(
    code: String = "locked",
    message: String = "The resource is temporarily unavailable",
    val retryAfter: Instant,
) :
    ApiException(
        status = HttpStatusCode.Locked,
        code = code,
        title = "Try Later",
        message = message,
    )

class RecoveryWaitException(retryAfter: Instant) :
    LockedException(
        code = "recovery_wait",
        message =
            "The invite cannot be generated yet because the recovery request wait period has not elapsed",
        retryAfter,
    )
