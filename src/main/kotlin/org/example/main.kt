package org.example

public fun main(): Unit {
  /*runBlocking(Dispatchers.Default) {
    val config = envConfig()
    module(config).use { module ->
      embeddedServer(
        Netty,
        port = config.http.port,
        host = config.http.host,
        parentCoroutineContext = coroutineContext,
      ) { app(module) }
        .start(wait = true)
    }
  }*/
}
