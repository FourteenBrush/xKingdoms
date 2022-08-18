package me.fourteendoggo.xkingdoms.storage.persistence;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.utils.LazyValue;
import me.fourteendoggo.xkingdoms.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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

        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
    }

    /**
     * Tries to execute database patches (if any), the patches file should be included in the jar
     * @return true if patches were found and executed or no patches were found, false if something caused an exception
     */
    public boolean executePatches(Logger logger) {
        String setup;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db-patch.sql")) {
            if (is == null) return true;
            setup = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read the db-patch.sql file, patches were not executed", e);
            return false;
        }

        String[] queries = setup.split(";");
        try (Connection conn = lazyConnection.get()) {
            for (String query : queries) {
                if (query.isBlank()) continue;
                executeRawQuery(conn, query);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query for database patch, is there an SQL syntax error?", e);
            return false;
        }
        logger.info("Applied patches to the database");
        return true;
    }

    protected void executeAll(String... queries) {
        try (lazyConnection) {
            for (String query : queries) {
                executeRawQuery(lazyConnection.get(), query);
            }
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }

    protected void executeRawQuery(Connection conn, String query) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.execute();
        }
    }

    protected <T> T withConnection(String sql, ThrowingBiFunction<Connection, PreparedStatement, T> function, Object... placeholders) {
        try (lazyConnection; PreparedStatement ps = lazyConnection.get().prepareStatement(sql)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(lazyConnection.get(), ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
        return null;
    }

    protected void withConnection(String sql, ThrowingBiConsumer<Connection, PreparedStatement> consumer, Object... placeholders) {
        try (lazyConnection; PreparedStatement ps = lazyConnection.get().prepareStatement(sql)) {
            fillPlaceholders(ps, placeholders);
            consumer.accept(lazyConnection.get(), ps);
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }


    /*
    WARNING: THESE DO NOT CLOSE THE CONNECTION
     */

    protected void withConnection(String sql, Connection conn, ThrowingConsumer<PreparedStatement> consumer, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                case UUID u -> {
                    ps.setBytes(i, uuidToBytes(u));
                }
                default -> throw new IllegalStateException("the programmer was too lazy to cover all possible outcomes");
            }
        }
    }

    protected byte[] uuidToBytes(UUID id) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits());

        return bytes;
    }

    public void disconnect() {
        lazyConnection.forceClose();
    }

    public abstract void connect();

    protected static class LazyConnection extends LazyValue<Connection> implements AutoCloseable {
        private final boolean autoCloseable;

        public LazyConnection(ThrowingSupplier<Connection> connectionSupplier, boolean autoClosable) {
            super(connectionSupplier);
            this.autoCloseable = autoClosable;
        }

        @Override
        public void close() {
            try {
                if (autoCloseable) {
                    get().close();
                }
            } catch (SQLException e) {
                Utils.sneakyThrow(e);
            }
        }

        public void forceClose() {
            try {
                Connection conn = get();
                if (!conn.isClosed()) { // some drivers don't like to be closed twice
                    conn.close();
                }
            } catch (SQLException e) {
                Utils.sneakyThrow(e);
            }
        }
    }

    @FunctionalInterface
    protected interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
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
