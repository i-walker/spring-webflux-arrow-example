package io.github.iwalker.services

import arrow.fx.coroutines.Resource
import com.zaxxer.hikari.HikariDataSource
import io.github.iwalker.config.Config
import io.github.iwalker.config.envDataSource
import io.github.iwalker.config.hikari
import io.github.iwalker.util.queryOneOrNull

public interface DatabasePool {
  public fun isRunning(): Boolean
  public suspend fun version(): String?
}

internal fun databasePool(hikari: HikariDataSource): DatabasePool =
  object : DatabasePool {
    override fun isRunning(): Boolean = hikari.isRunning
    override suspend fun version(): String? =
      hikari.queryOneOrNull("SHOW server_version;") { string() }
  }

internal fun databasePool(config: Config.DataSource = envDataSource()): Resource<DatabasePool> =
  hikari(config).map { databasePool(it) }
