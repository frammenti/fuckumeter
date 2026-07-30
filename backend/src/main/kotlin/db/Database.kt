package dev.frammenti.fuckumeter.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway

class Database(config: DatabaseConfig) : DatabaseContext {
    val ds: DataSource = createDataSource(config)

    init {
        Flyway.configure().dataSource(ds).load().migrate()
    }

    private fun createDataSource(config: DatabaseConfig): HikariDataSource {
        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                driverClassName = "org.postgresql.Driver"
            }

        return HikariDataSource(hikariConfig)
    }

    override fun <T> session(block: Session.() -> T): T =
        sessionOf(ds).use { session ->
            session.block()
        }

    override fun <T> transaction(block: Session.() -> T): T =
        sessionOf(ds).use { session ->
            session.transaction {
                session.block()
            }
        }

    override fun sql(statement: String, vararg params: Pair<String, Any?>) =
        queryOf(statement.trimIndent(), mapOf(*params))
}
