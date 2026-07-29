package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)