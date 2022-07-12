package me.fourteendoggo.xkingdoms.storage.database.impl;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.storage.database.Database;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class H2Database implements Database {
    private static final @Language("SQL") String[] TABLE_SETUP = new String[]{
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid UUID PRIMARY KEY,
                level INT NOT NULL DEFAULT 0
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS homes (
                id UUID PRIMARY KEY,
                owner UUID,
                name VARCHAR(40),
                location_world UUID NOT NULL,
                location_x DOUBLE PRECISION NOT NULL,
                location_y DOUBLE PRECISION NOT NULL,
                location_z DOUBLE PRECISION NOT NULL,
                location_yaw FLOAT NOT NULL,
                location_pitch FLOAT NOT NULL
            );"""
    };

    private final ConnectionProvider connProvider;

    public H2Database(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    @Override
    public void connect() {
        try (Connection conn = connProvider.getConnection()) {
            for (String query : TABLE_SETUP) {
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void disconnect() {
        connProvider.close();
    }

    private void withConnection(ThrowingConsumer<Connection> consumer) {
        try (Connection conn = connProvider.getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    private <T> T withConnection(String statement, ThrowingBiFunction<Connection, PreparedStatement, T> function, Object... placeholders) {
        try (Connection conn = connProvider.getConnection(); PreparedStatement ps =
             conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(conn, ps);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }

    private <T> T withConnection(String statement, Connection conn,  ThrowingFunction<PreparedStatement, T> function, Object... placeholders) {
        try (conn; PreparedStatement ps =
              conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            return function.apply(ps);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
    }


    private void withConnection(String statement, ThrowingBiConsumer<Connection, PreparedStatement> consumer, Object... placeholders) {
        try (Connection conn = connProvider.getConnection(); PreparedStatement ps =
             conn.prepareStatement(statement)) {
            fillPlaceholders(ps, placeholders);
            consumer.accept(conn, ps);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    private void fillPlaceholders(PreparedStatement ps, Object... placeholders) throws SQLException {
        for (int i = 0; i < placeholders.length; i++) {
            switch (placeholders[i]) {
                case String s -> ps.setString(i, s);
                case Boolean b -> ps.setBoolean(i, b);
                case Byte b -> ps.setByte(i, b);
                case Short s -> ps.setShort(i, s);
                case Integer integer -> ps.setInt(i, integer);
                case Long l -> ps.setLong(i, l);
                case UUID u -> ps.setString(i, u.toString());
                case Date d -> ps.setDate(i, d);
                default -> throw new IllegalStateException("the programmer was too lazy to cover all possible outcomes");
            }
        }
    }

    @Override
    public KingdomPlayer loadUser(UUID id) {
        return withConnection("SELECT * FROM players WHERE uuid=?;", (conn, ps) -> {
            ResultSet rs = ps.executeQuery();
            int level = rs.next() ? rs.getInt("level") : 0;
            List<Home> homes = fetchHomes(conn, id);

            return new KingdomPlayer(id, new PlayerData(level, homes));
        }, id);
    }

    private List<Home> fetchHomes(Connection conn, UUID owner) {
        return withConnection("SELECT * FROM homes WHERE owner=?;", conn, ps -> {
            ResultSet rs = ps.executeQuery();
            List<Home> homes = new ArrayList<>();
            while (rs.next()) {
                homes.add(Home.fromResultSet(rs));
            }
            return homes;
        }, owner);
    }

    @Override
    public void saveUser(KingdomPlayer player) {
        withConnection("MERGE INTO players KEY(uuid, level) VALUES(?,?);", (conn, ps) -> {
            ps.executeUpdate();
        }, player.getUniqueId(), player.getData().getLevel());
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface ThrowingBiConsumer<T, U> {
        void accept(T t, U u) throws SQLException;
    }

    @FunctionalInterface
    private interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws SQLException;
    }
}
