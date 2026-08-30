package dev.frammenti.fuckumeter.extensions

import dev.frammenti.fuckumeter.domain.Invite
import dev.frammenti.fuckumeter.domain.Invite.WithCode

fun <I : Invite> I.withCode(code: String = code()) = WithCode(this, code)
