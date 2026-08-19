package repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DatabaseContextTest : RepositoryTest() {
    private val ds = database.ds

    private fun Session.backendPid(): Int =
        single(queryOf("SELECT pg_backend_pid() AS pid")) {
            it.int("pid")
        }!!

    private fun Session.insertRow(name: String) {
        update(queryOf("INSERT INTO users (name) values (?)", name))
    }

    private fun countRows(): Int =
        sessionOf(ds).single(queryOf("SELECT COUNT(*) as c from users")) {
            it.int("c")
        }!!

    private fun getNames(): List<String> =
        sessionOf(ds).list(
            queryOf("SELECT name FROM users ORDER BY created_at")
        ) {
            it.string("name")
        }

    // Session

    @Test
    suspend fun `concurrent sessions use distinct connections`() =
        coroutineScope {
            val pids =
                (1..3)
                    .map {
                        async { database.session { backendPid() } }
                    }
                    .awaitAll()
            assertEquals(pids.size, pids.toSet().size)
        }

    @Test
    suspend fun `nested session calls share one connection`() = coroutineScope {
        val (pidOuter, pidInner) =
            database.session {
                val outer = backendPid()
                val inner = database.session {
                    backendPid()
                } // e.g. a repo call made from inside a service session
                outer to inner
            }
        assertEquals(pidOuter, pidInner)
    }

    @Test
    suspend fun `nested session inside a transaction shares its connection`() =
        coroutineScope {
            val (pidTx, pidSession) =
                database.transaction {
                    val tx = backendPid()
                    val session = database.session {
                        backendPid()
                    } // e.g. a repo call made from inside a service tx
                    tx to session
                }
            assertEquals(pidTx, pidSession)
        }

    // Transaction

    @Test
    suspend fun `concurrent transactions use distinct connections`() =
        coroutineScope {
            val pids =
                (1..3)
                    .map {
                        async { database.transaction { backendPid() } }
                    }
                    .awaitAll()
            assertEquals(pids.size, pids.toSet().size)
        }

    @Test
    suspend fun `nested transaction calls share one connection`() =
        coroutineScope {
            val (pidOuter, pidInner) =
                database.transaction {
                    val outer = backendPid()
                    val inner = database.transaction {
                        backendPid()
                    } // e.g. service B called from inside service A's tx
                    outer to inner
                }
            assertEquals(pidOuter, pidInner)
        }

    @Test
    suspend fun `nested transaction inside a session shares its connection`() =
        coroutineScope {
            val (pidSession, pidTx) =
                database.session {
                    val session = backendPid()
                    val tx = database.transaction {
                        backendPid()
                    }
                    session to tx
                }
            assertEquals(pidSession, pidTx)
        }

    // Transaction inside transaction

    @Test
    suspend fun `nested transactions commit together`() = coroutineScope {
        database.transaction {
            insertRow("outer")
            database.transaction {
                insertRow("inner")
            }
        }
        assertEquals(listOf("outer", "inner"), getNames())
    }

    @Test
    suspend fun `inner transaction failure rolls back an outer transaction`() =
        coroutineScope {
            assertThrows<RuntimeException> {
                database.transaction {
                    insertRow("outer")
                    database.transaction {
                        insertRow("inner")
                        throw RuntimeException("boom")
                    }
                }
            }
            assertEquals(0, countRows())
        }

    @Test
    suspend fun `outer transaction failure rolls back an inner transaction`() =
        coroutineScope {
            assertThrows<RuntimeException> {
                database.transaction {
                    insertRow("outer")
                    database.transaction {
                        insertRow("inner") // should not commit prematurely
                    }
                    throw RuntimeException("boom after inner succeeded")
                }
            }
            assertEquals(0, countRows())
        }

    // Transaction inside session

    @Test
    suspend fun `inner transaction auto-commits like other session blocks`() =
        coroutineScope {
            database.session {
                insertRow("pre-transaction")
                database.transaction {
                    insertRow("transaction")
                }
                insertRow("post-transaction")
            }
            assertEquals(
                listOf("pre-transaction", "transaction", "post-transaction"),
                getNames(),
            )
        }

    @Test
    suspend fun `inner transaction failure rolls back the transaction only`() =
        coroutineScope {
            assertThrows<RuntimeException> {
                database.session {
                    insertRow(
                        "pre-transaction"
                    ) // not transactional: auto-committed immediately
                    database.transaction {
                        insertRow("transaction")
                        throw RuntimeException("boom")
                    }
                }
            }
            // pre-transaction insert already committed on its own
            assertEquals(listOf("pre-transaction"), getNames())
        }
}
