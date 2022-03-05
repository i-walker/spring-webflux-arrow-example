package io.github.iwalker.services

public interface DatabasePool {
  public fun isRunning(): Boolean
  public suspend fun version(): String?
}
