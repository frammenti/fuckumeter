package dev.frammenti.fuckumeter.exceptions

import io.ktor.http.HttpStatusCode

sealed class GoneException(
    code: String = "gone",
    message: String = "The resource is permanently gone",
) :
    ApiException(
        status = HttpStatusCode.Gone,
        code = code,
        title = "Unavailable",
        message = message,
    )

class InviteExpiredException :
    GoneException(
        "invite_expired",
        "The invite has expired",
    )

class InviteConsumedException :
    GoneException(
        "invite_consumed",
        "The invite has already been consumed",
    )

class InviteRevokedException :
    GoneException(
        "invite_revoked",
        "Access to the invite was revoked",
    )

class InviteRevokedByPartnerException :
    GoneException(
        "invite_revoked_by_partner",
        "Access to the invite was revoked by a partner",
    )
