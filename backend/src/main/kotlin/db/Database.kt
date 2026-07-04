package dev.frammenti.fuckumeter.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import kotliquery.Session
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object Database {

    lateinit var ds: DataSource
        private set

    fun initialize(application: Application) {
        ds = createDataSource(application.environment.config)

        Flyway.configure().dataSource(ds).load().migrate()
    }

    private fun createDataSource(config: ApplicationConfig): HikariDataSource {
        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl = config.property("postgres.url").getString()
                username = config.property("postgres.user").getString()
                password = config.property("postgres.password").getString()

                driverClassName = "org.postgresql.Driver"
            }

        return HikariDataSource(hikariConfig)
    }

    inline fun <T> session(block: Session.() -> T): T =
        sessionOf(ds).use { session ->
            session.block()
        }

    inline fun <T> transaction(block: Session.() -> T): T =
        sessionOf(ds).use { session ->
            session.transaction {
                session.block()
            }
        }
}
