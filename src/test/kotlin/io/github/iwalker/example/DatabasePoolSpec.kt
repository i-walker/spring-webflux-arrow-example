package io.github.iwalker.example

import io.github.iwalker.PostgresContainer
import io.github.iwalker.resource
import io.github.iwalker.services.databasePool
import io.kotest.core.spec.style.StringSpec

class DatabasePoolSpec :
  StringSpec({
    "test DB" {
      val databasePool by resource(databasePool(PostgresContainer.config()))

      println(databasePool.isRunning())
      val version = databasePool.version()
      println(version)
    }
  })
