package dev.frammenti.fuckumeter.extensions

fun Int.expectOne() {
    when (this) {
        1 -> Unit
        0 ->
            throw NoSuchElementException(
                "Expected one affected row, but none were affected."
            )
        else ->
            throw IllegalStateException("Expected one affected row, got $this.")
    }
}
