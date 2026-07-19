import os

with open('backend/shared/database/DatabaseConnector.kt', 'r') as f:
    content = f.read()

old_config = """        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            this.jdbcUrl = jdbcUrl
            username = user
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }"""

new_config = """        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            this.jdbcUrl = jdbcUrl
            username = user
            this.password = password
            maximumPoolSize = 20
            minimumIdle = 5
            idleTimeout = 600000
            connectionTimeout = 30000
            maxLifetime = 1800000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }"""

content = content.replace(old_config, new_config)

with open('backend/shared/database/DatabaseConnector.kt', 'w') as f:
    f.write(content)
