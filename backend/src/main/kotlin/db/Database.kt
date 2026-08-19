package dev.frammenti.fuckumeter.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway

class Database(config: DatabaseConfig) : DatabaseContext {
    val ds: DataSource = createDataSource(config)

    init {
        Flyway.configure().dataSource(ds).load().migrate()
    }

    constructor() : this(DatabaseConfig())

    private fun createDataSource(config: DatabaseConfig): HikariDataSource {
        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                driverClassName = "org.postgresql.Driver"
                leakDetectionThreshold =
                    2_000 // warns if a connection is held > 2s and not returned
            }

        return HikariDataSource(hikariConfig)
    }

    /** Session carried through the coroutine context. */
    private class SessionContext(val session: Session) :
        AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<SessionContext>
    }

    /**
     * Runs [block] against the context session if one exists, else opens a new
     * one.
     */
    override suspend fun <T> session(block: suspend Session.() -> T): T {
        currentCoroutineContext()[SessionContext]?.let {
            return it.session.block()
        }

        return withContext(Dispatchers.IO) {
            sessionOf(ds, strict = true).use { session ->
                withContext(SessionContext(session = session)) {
                    session.block()
                }
            }
        }
    }

    /**
     * Runs [block] transactionally.
     * - No session -> opens one and starts a transaction on it.
     * - Session already transactional -> joins it directly, no new
     *   commit/rollback boundary (prevents a nested transaction from committing
     *   prematurely).
     * - Session exists but isn't transactional -> opens a transaction inside.
     */
    override suspend fun <T> transaction(block: suspend Session.() -> T): T {
        currentCoroutineContext()[SessionContext]?.let {
            return if (it.session is TransactionalSession) {
                it.session.block() // already transactional -> join directly
            } else {
                // session exists but isn't transactional -> promote it
                it.session.transaction { tx ->
                    tx.block()
                }
            }
        }

        return withContext(Dispatchers.IO) {
            // `session.use` is needed to auto-close the connection
            sessionOf(ds, strict = true).use { session ->
                session.transaction { tx ->
                    withContext(SessionContext(session = tx)) {
                        tx.block()
                    }
                }
            }
        }
    }

    override fun sql(statement: String, vararg params: Pair<String, Any?>) =
        queryOf(statement.trimIndent(), mapOf(*params))
}
