package org.example.config

public data class Config(val dataSource: DataSource, val http: Http) {
  public data class Http(val host: String, val port: Int)

  public data class DataSource(
    val url: String,
    val username: String,
    val password: String,
    val driver: String = "org.postgresql.Driver"
  )
}

private const val DEFAULT_PORT: Int = 8080

public fun envConfig(): Config = Config(envDataSource(), envHttp())

public fun envHttp(): Config.Http =
  Config.Http(
    host = System.getenv("HOST") ?: "0.0.0.0",
    port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: DEFAULT_PORT
  )

public fun envDataSource(): Config.DataSource =
  Config.DataSource(
    url = System.getenv("POSTGRES_URL")
      ?: "jdbc:postgresql://localhost:5432/spring-arrow-example-database",
    username = System.getenv("POSTGRES_USERNAME") ?: "postgres",
    password = System.getenv("POSTGRES_PASSWORD") ?: "postgres",
  )
