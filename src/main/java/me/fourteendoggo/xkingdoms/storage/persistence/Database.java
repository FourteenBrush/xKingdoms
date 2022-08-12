package me.fourteendoggo.xkingdoms.storage.persistence;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.utils.LazyValue;
import me.fourteendoggo.xkingdoms.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Database {
    protected final HikariDataSource dataSource; // nullable
    protected final LazyConnection lazyConnection;

    public Database(LazyConnection lazyConnection) {
        this.dataSource = null;
        this.lazyConnection = lazyConnection;
    }

    public Database() {
        this.dataSource = new HikariDataSource();
        this.lazyConnection = new LazyConnection(dataSource::getConnection, true);

        dataSource.setPoolName("[xKingdoms - connection pool]");
        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
    }

    public void executePatches(Logger logger) {
        String setup;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db-patch.sql")) {
            if (is == null) return;
            setup = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read the db-patch.sql file, patches were not executed", e);
            return;
        }

        String[] queries = setup.split(";");
        try (Connection conn = getConnection()) {
            for (String query : queries) {
                if (query.isBlank()) continue;
                executeRawQuery(conn, query);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query for database patch, is there an SQL syntax error?", e);
            return;
        }
        logger.info("Applied patches to the database");
    }

    protected <T> T withConnection2(String sql, ThrowingBiFunction<Connection, PreparedStatement, T> function, Object... placeholders) {
        try (lazyConnection; PreparedStatement ps = lazyConnection.get().prepareStatement(sql)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(getConnection(), ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
        return null;
    }

    protected <T> T withConnection(String statement, ThrowingBiFunction<Connection, PreparedStatement, T> function, Object... placeholders) {
        try (Connection conn = getConnection(); PreparedStatement ps =
                conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(conn, ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
        return null;
    }

    protected void withConnection2(String sql, ThrowingBiConsumer<Connection, PreparedStatement> consumer, Object... placeholders) {
        try (lazyConnection; PreparedStatement ps = lazyConnection.get().prepareStatement(sql)) {
            fillPlaceholders(ps, placeholders);
            consumer.accept(getConnection(), ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }

    protected void withConnection(String statement, ThrowingBiConsumer<Connection, PreparedStatement> consumer, Object... placeholders) {
        try (Connection conn = getConnection(); PreparedStatement ps =
                conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            consumer.accept(conn, ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }


    /*
    WARNING: DOES NOT CLOSE THE CONNECTION
     */
    protected <T> T withConnection(String statement, Connection conn, ThrowingFunction<PreparedStatement, T> function, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
        return null;
    }

    protected void withConnection(String statement, Connection conn, ThrowingConsumer<PreparedStatement> consumer, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            consumer.accept(ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }

    private void fillPlaceholders(PreparedStatement ps, Object... placeholders) throws SQLException {
        for (int i = 1; i < placeholders.length + 1; i++) {
            switch (placeholders[i - 1]) {
                case String s -> ps.setString(i, s);
                case Boolean b -> ps.setBoolean(i, b);
                case Byte b -> ps.setByte(i, b);
                case Short s -> ps.setShort(i, s);
                case Integer integer -> ps.setInt(i, integer);
                case Long l -> ps.setLong(i, l);
                case Float f -> ps.setFloat(i, f);
                case UUID u -> ps.setString(i, u.toString());
                case Date d -> ps.setDate(i, d);
                default -> throw new IllegalStateException("the programmer was too lazy to cover all possible outcomes");
            }
        }
    }

    protected void executeRawQuery(Connection conn, String query) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.execute();
        }
    }

    protected Connection getConnection() throws SQLException {
        return lazyConnection.get();
    }

    public void disconnect() {
        try {
            lazyConnection.forceClose();
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }

    protected static class LazyConnection extends LazyValue<Connection> implements AutoCloseable {
        private final boolean autoCloseable;

        public LazyConnection(ThrowingSupplier<Connection> connectionSupplier, boolean autoClosable) {
            super(connectionSupplier);
            this.autoCloseable = autoClosable;
        }

        @Override
        public Connection get() {
            Connection conn = super.get();
            try {
                System.out.println("Closed: " + conn.isClosed());
            } catch (SQLException e) {

            }
            return conn;
        }

        @Override
        public void close() throws SQLException {
            if (autoCloseable) {
                get().close();
                System.out.println("DEBUG: closed connection"); // TODO: remove
            }
        }

        public void forceClose() throws SQLException {
            Connection conn = get();
            if (conn.isClosed()) { // some drivers don't like to be closed twice
                conn.close();
                System.out.println("DEBUG: closed connection"); // TODO: remove
            }
        }
    }

    @FunctionalInterface
    protected interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    protected interface ThrowingBiConsumer<T, U> {
        void accept(T t, U u) throws SQLException;
    }

    @FunctionalInterface
    protected interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws SQLException;
    }
}
