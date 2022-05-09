package io.github.iwalker

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.fromCloseable
import com.github.dockerjava.api.DockerClient
import io.github.iwalker.config.Config
import io.kotest.core.test.TestScope
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget

class PostgresContainer private constructor() :
  PostgreSQLContainer<Nothing>("postgres:14.1-alpine") {

  private fun config(): Config.DataSource =
    Config.DataSource(
      host,
      getMappedPort(POSTGRESQL_PORT),
      jdbcUrl,
      username,
      password,
      driverClassName
    )

  companion object {
    fun create(waitStrategy: AbstractWaitStrategy? = null): PostgresContainer =
      waitStrategy?.let { strategy -> instance.also { it.waitingFor(strategy) } } ?: instance

    fun config(): Config.DataSource = instance.config()

    private val instance by lazy {
      PostgresContainer()
        .apply {
          withNetwork(null)
          withNetworkAliases(null)
          withNetworkMode(null)
          withExposedPorts(POSTGRESQL_PORT)
          withDatabaseName("spring-arrow-example-database")
          withReuse(true)
          withCreateContainerCmdModifier { it.withHostName("io.github.iwalker.PostgresContainer") }
        }
        .also { it.start() }
    }
  }
}

fun TestScope.waitStrategy(
  f: suspend CoroutineScope.(client: Resource<DockerClient>, target: WaitStrategyTarget) -> Unit
): AbstractWaitStrategy =
  object : AbstractWaitStrategy() {
    override fun waitUntilReady() {
      launch(Dispatchers.IO) {
        val client =
          Resource.fromCloseable { DockerClientFactory.instance().client().shouldNotBeNull() }
        f(client, waitStrategyTarget)
      }
    }
  }
