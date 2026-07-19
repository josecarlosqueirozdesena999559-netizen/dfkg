package com.decisoes.shared.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

object DatabaseConnector {
    private val logger = LoggerFactory.getLogger(DatabaseConnector::class.java)
    private var dataSource: HikariDataSource? = null

    fun init() {
        val useH2 = System.getenv("USE_H2") == "true" || System.getenv("DB_HOST").isNullOrBlank()
        
        val (jdbcUrl, user, password, driver) = if (useH2) {
            val dbPath = "./decisoes_db"
            logger.info("Using H2 database at {}", dbPath)
            listOf("jdbc:h2:file:$dbPath;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH", "sa", "", "org.h2.Driver")
        } else {
            val host = System.getenv("DB_HOST") ?: "localhost"
            val port = System.getenv("DB_PORT") ?: "5432"
            val databaseName = System.getenv("DB_NAME") ?: "decisoes"
            val u = System.getenv("DB_USER") ?: "postgres"
            val p = System.getenv("DB_PASSWORD") ?: "postgres"
            listOf("jdbc:postgresql://$host:$port/$databaseName", u, p, "org.postgresql.Driver")
        }

        logger.info("Initializing database connection to: {}", jdbcUrl)

        val config = HikariConfig().apply {
            driverClassName = driver
            this.jdbcUrl = jdbcUrl
            username = user
            this.password = password
            maximumPoolSize = 20
            minimumIdle = 5
            idleTimeout = 600000
            connectionTimeout = 30000
            maxLifetime = 1800000
            isAutoCommit = false
            if (!useH2) {
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            validate()
        }

        dataSource = HikariDataSource(config)
        Database.connect(dataSource!!)

        // Run migrations
        runFlyway(jdbcUrl, user, password)
    }

    private fun runFlyway(url: String, user: String, pass: String) {
        try {
            logger.info("Running Flyway migrations...")
            val flyway = Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
            flyway.migrate()
            logger.info("Flyway migrations completed successfully")
        } catch (e: Exception) {
            logger.error("Failed to run Flyway migrations", e)
            throw e
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val tx = org.jetbrains.exposed.sql.transactions.TransactionManager.currentOrNull()
        return if (tx != null) {
            block()
        } else {
            newSuspendedTransaction(Dispatchers.IO) { block() }
        }
    }

    fun checkHealth(): Boolean {
        return try {
            dataSource?.connection?.use { conn ->
                conn.isValid(2)
            } ?: false
        } catch (e: Exception) {
            logger.error("Database health check failed", e)
            false
        }
    }
}
