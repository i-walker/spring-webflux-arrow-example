package io.github.iwalker

import io.github.iwalker.config.envConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

public fun main(): Unit =
  runBlocking(Dispatchers.Default) {
    val config = envConfig()
    /*module(config).use { module ->
      embeddedServer(
        ServerProperties.Netty,
        port = config.http.port,
        host = config.http.host,
        parentCoroutineContext = coroutineContext,
      ) { app(module) }
        .start(wait = true)
    }*/
  }
