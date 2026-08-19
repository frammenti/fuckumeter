package dev.frammenti.fuckumeter.db

import kotliquery.Query
import kotliquery.Session

interface DatabaseContext {
    suspend fun <T> session(block: suspend Session.() -> T): T

    suspend fun <T> transaction(block: suspend Session.() -> T): T

    fun sql(statement: String, vararg params: Pair<String, Any?>): Query
}
