package me.fourteendoggo.xkingdoms.storage.persistence

import com.zaxxer.hikari.HikariDataSource
import org.intellij.lang.annotations.Language
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

// TODO: fix this craziness
abstract class Database {
    private val dataSource: HikariDataSource?
    protected var _connection: Connection
    private val connection: Connection
        get() = if (_connection.isClosed) {
            dataSource!!.connection
        } else {
            _connection
        }

    init {
        dataSource = HikariDataSource()
        _connection = dataSource.connection
        dataSource.addDataSourceProperty("useSSL", false)
        dataSource.addDataSourceProperty("cachePrepStmts", true)
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250)
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048)
    }

    /**
     * Tries to execute database patches (if any), the patches file should be included in the jar
     *
     * @return true if patches were found and executed or no patches were found, false if something caused an exception
     */
    fun executePatches(logger: Logger): Boolean {
        var setup: String
        try {
            javaClass.getClassLoader().getResourceAsStream("db-patch.sql").use { stream ->
                if (stream == null) return true
                setup = String(stream.readAllBytes(), StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Failed to read the db-patch.sql file, patches were not executed", e)
            return false
        }
        val queries = setup.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            this.connection.use { conn ->
                for (@Language("SQL") query in queries) {
                    if (query.isBlank()) continue
                    executeRawQuery(conn, query)
                }
            }
        } catch (e: SQLException) {
            logger.log(Level.SEVERE, "Failed to execute query for database patch, is there an SQL syntax error?", e)
            return false
        }
        logger.info("Applied patches to the database")
        return true
    }

    protected fun executeAll(queries: Array<String>) {
        connection.use {
            for (@Language("SQL") query in queries) {
                executeRawQuery(it, query)
            }
        }
    }

    private fun executeRawQuery(conn: Connection, @Language("SQL") query: String) {
        conn.prepareStatement(query).use { ps -> ps.execute() }
    }

    protected fun <T> withConnection(
        @Language("SQL") sql: String,
        vararg placeholders: Any,
        func: (Connection, PreparedStatement) -> T?,
    ): T? = connection.use { conn ->
        conn.prepareStatement(sql).use { ps ->
            fillPlaceholders(ps, placeholders)
            return@withConnection func(conn, ps)
        }
    }

    protected fun withConnection(
        @Language("SQL") sql: String,
        vararg placeholders: Any,
        func: (Connection, PreparedStatement) -> Unit,
    ) = connection.use { conn ->
        conn.prepareStatement(sql).use { ps ->
            fillPlaceholders(ps, placeholders)
            func(conn, ps)
        }
    }

    /*
    WARNING: THESE DO NOT CLOSE THE CONNECTION
     */
    protected fun withConnection(
        @Language("SQL") sql: String,
        conn: Connection,
        vararg placeholders: Any,
        func: (PreparedStatement) -> Unit) {
        conn.prepareStatement(sql).use { ps ->
            fillPlaceholders(ps, placeholders)
            func(ps)
        }
    }

    private fun fillPlaceholders(ps: PreparedStatement, vararg placeholders: Any) {
        for ((i, placeholder) in placeholders.withIndex()) {
            ps.setObject(i + 1, placeholder)
        }
    }

    protected fun uuidToBytes(id: UUID): ByteArray {
        return ByteBuffer.allocate(16)
                .putLong(id.mostSignificantBits)
                .putLong(id.leastSignificantBits)
                .flip().array()
    }

    protected fun uuidFromBytes(bytes: ByteArray): UUID {
        val buf = ByteBuffer.wrap(bytes)
        return UUID(buf.getLong(), buf.getLong())
    }

    fun disconnect() {
        // TODO: close connection without opening a new one
        dataSource?.close()
    }

    abstract fun connect()
}
