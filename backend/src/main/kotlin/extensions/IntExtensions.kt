package dev.frammenti.fuckumeter.extensions

fun Int.expectOne() {
    check(this == 1) {
        "Expected exactly one affected row, got $this."
    }
}
