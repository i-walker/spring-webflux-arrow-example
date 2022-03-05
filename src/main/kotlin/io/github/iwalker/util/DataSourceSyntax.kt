package io.github.iwalker.util

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.fromAutoCloseable
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource

/**
 * credits goes to
 * https://github.com/nomisRev/ktor-arrow-example/blob/main/src/main/kotlin/io/github/nomisrev/utils/DataSourceSyntax.kt
 */
public fun DataSource.connection(): Resource<Connection> =
  Resource.fromAutoCloseable { connection }

public fun DataSource.prepareStatement(
  sql: String,
  binders: (SqlPreparedStatement.() -> Unit)? = null
): Resource<PreparedStatement> =
  connection().flatMap { connection ->
    Resource.fromAutoCloseable {
      connection.prepareStatement(sql).apply {
        if (binders != null) SqlPreparedStatement(this).binders()
      }
    }
  }

public suspend fun DataSource.query(sql: String): Unit =
  prepareStatement(sql)
    .flatMap { preparedStatement -> Resource({ preparedStatement.executeUpdate() }, { _, _ -> }) }
    .use {}

public suspend fun <A> DataSource.queryOneOrNull(
  sql: String,
  binders: (SqlPreparedStatement.() -> Unit)? = null,
  mapper: SqlCursor.() -> A
): A? =
  prepareStatement(sql)
    .flatMap { preparedStatement ->
      Resource.fromAutoCloseable {
        preparedStatement
          .apply { if (binders != null) SqlPreparedStatement(this).binders() }
          .executeQuery()
      }
    }
    .use { rs -> if (rs.next()) mapper(SqlCursor(rs)) else null }

public suspend fun <A> DataSource.queryAsList(
  sql: String,
  binders: (SqlPreparedStatement.() -> Unit)? = null,
  mapper: SqlCursor.() -> A?
): List<A> =
  prepareStatement(sql)
    .flatMap { preparedStatement ->
      Resource.fromAutoCloseable {
        preparedStatement
          .apply { if (binders != null) SqlPreparedStatement(this).binders() }
          .executeQuery()
      }
    }
    .use { rs ->
      val buffer = mutableListOf<A>()
      while (rs.next()) {
        mapper(SqlCursor(rs))?.let(buffer::add)
      }
      buffer
    }

public class SqlPreparedStatement(private val preparedStatement: PreparedStatement) {
  private var index: Int = 1

  public fun bind(short: Short?): Unit = bind(short?.toLong())
  public fun bind(byte: Byte?): Unit = bind(byte?.toLong())
  public fun bind(int: Int?): Unit = bind(int?.toLong())
  public fun bind(char: Char?): Unit = bind(char?.toString())

  public fun bind(bytes: ByteArray?): Unit =
    if (bytes == null) preparedStatement.setNull(index++, Types.BLOB)
    else preparedStatement.setBytes(index++, bytes)

  public fun bind(long: Long?): Unit =
    if (long == null) preparedStatement.setNull(index++, Types.INTEGER)
    else preparedStatement.setLong(index++, long)

  public fun bind(double: Double?): Unit =
    if (double == null) preparedStatement.setNull(index++, Types.REAL)
    else preparedStatement.setDouble(index++, double)

  public fun bind(string: String?): Unit =
    if (string == null) preparedStatement.setNull(index++, Types.VARCHAR)
    else preparedStatement.setString(index++, string)
}

public class SqlCursor(private val resultSet: ResultSet) {
  private var index: Int = 1
  public fun int(): Int? = long()?.toInt()
  public fun string(): String? = resultSet.getString(index++)
  public fun bytes(): ByteArray? = resultSet.getBytes(index++)
  public fun long(): Long? = resultSet.getLong(index++).takeUnless { resultSet.wasNull() }
  public fun double(): Double? = resultSet.getDouble(index++).takeUnless { resultSet.wasNull() }
  public fun nextRow(): Boolean = resultSet.next()
}
