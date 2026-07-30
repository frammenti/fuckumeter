package dev.frammenti.fuckumeter.db

import kotliquery.Query
import kotliquery.Session

interface DatabaseContext {
    fun <T> session(block: Session.() -> T): T

    fun <T> transaction(block: Session.() -> T): T

    fun sql(statement: String, vararg params: Pair<String, Any?>): Query
}
