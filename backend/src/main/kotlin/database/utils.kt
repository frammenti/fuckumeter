package dev.frammenti.fuckumeter.database

import kotliquery.queryOf

fun sql(statement: String, vararg params: Pair<String, Any?>) =
    queryOf(statement.trimIndent(), mapOf(*params))
