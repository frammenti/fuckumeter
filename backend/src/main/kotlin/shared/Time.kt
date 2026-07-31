package dev.frammenti.fuckumeter.shared

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

object Time {
    var clock: Clock = Clock.systemUTC()

    fun now(): Instant = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
}
