package dev.frammenti.fuckumeter.security

import kotliquery.Row

data class Encrypted(
    val ciphertext: ByteArray,
    val nonce: ByteArray,
) {
    constructor(
        row: Row
    ) : this(
        row.bytes("code_ciphertext"),
        row.bytes("code_nonce"),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Encrypted

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}
