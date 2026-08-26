package dev.frammenti.fuckumeter.dto

import dev.frammenti.fuckumeter.exceptions.ApiException
import dev.frammenti.fuckumeter.exceptions.ConflictException
import dev.frammenti.fuckumeter.exceptions.LockedException
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val type: String, // internal code
    val status: HttpStatusCode, // http status code
    val title: String, // high-level error name (e.g. Validation Error)
    val detail: String, // error message
) {
    constructor(
        error: ApiException
    ) : this(
        type = error.code,
        status = error.status,
        title = error.title,
        detail = error.message,
    )
}

@Serializable
data class LockedErrorResponse(
    val type: String,
    val status: HttpStatusCode,
    val title: String,
    val detail: String,
    val retryAfter: Instant, // corresponds to retry-after header
) {
    constructor(
        error: LockedException
    ) : this(
        type = error.code,
        status = error.status,
        title = error.title,
        detail = error.message,
        retryAfter = error.retryAfter,
    )
}

@Serializable
data class ConflictErrorResponse(
    val type: String,
    val status: HttpStatusCode,
    val title: String,
    val detail: String,
    @EncodeDefault(NEVER) val cause: UUID? = null, // id of conflicting resource
) {
    constructor(
        error: ConflictException
    ) : this(
        type = error.code,
        status = error.status,
        title = error.title,
        detail = error.message,
        cause = error.causeId,
    )
}
