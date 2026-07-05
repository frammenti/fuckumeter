package dev.frammenti.fuckumeter.extensions

inline fun <reified T> Any?.expect(): T? {
    require(this == null || this is T) {
        "Expected ${T::class.qualifiedName}, got ${this?.let { it::class.qualifiedName }}"
    }

    @Suppress("UNCHECKED_CAST")
    return this as T?
}
