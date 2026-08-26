package dev.frammenti.fuckumeter.domain

sealed interface InternalId {
    data class WithId<out I : InternalId>(
        val obj: I,
        val id: Long,
    )
}
