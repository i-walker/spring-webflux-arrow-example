package org.example

import org.example.config.Config
import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer private constructor() :
  PostgreSQLContainer<Nothing>("postgres:14.1-alpine") {

  internal fun config(): Config.DataSource =
    Config.DataSource(jdbcUrl, username, password, driverClassName)

  companion object {
    fun create(): PostgresContainer = instance

    fun config(): Config.DataSource = instance.config()

    private val instance by lazy { PostgresContainer().also { it.start() } }
  }
}
