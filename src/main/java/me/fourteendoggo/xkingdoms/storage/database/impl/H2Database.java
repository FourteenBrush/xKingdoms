package me.fourteendoggo.xkingdoms.storage.database.impl;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.storage.database.Database;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
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
                owner UUID
                name VARCHAR(40),
                location_world UUID NOT NULL,
                location_x DOUBLE PRECISION NOT NULL,
                location_y DOUBLE PRECISION NOT NULL,
                location_z DOUBLE PRECISION NOT NULL,
                location_yaw FLOAT NOT NULL,
                location_pitch FLOAT NOT NULL,
                PRIMARY KEY (owner, name)
            );"""
    };
    private final ConnectionProvider connProvider;

    public H2Database(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    private void withConnection(ThrowingConsumer<Connection> consumer) {
        try (Connection conn = connProvider.getConnection()) {
            consumer.accept(conn);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    private <T> T withConnection(ThrowingFunction<Connection, T> function) {
        try (Connection conn = connProvider.getConnection()) {
            return function.apply(conn);
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
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

    @Override
    public KingdomPlayer loadUser(UUID id) {
        return withConnection(conn -> {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM players WHERE uuid=?;");
            ResultSet rs = ps.executeQuery();
            int level = 0;
            if (rs.next()) {
                level = rs.getInt("level");
            }
            List<Home> homes = fetchHomes(conn, id);

            return new KingdomPlayer(id, new PlayerData(level, homes));
        });





        /*
        try (Connection conn = connProvider.getConnection(); PreparedStatement loadUser =
                conn.prepareStatement("SELECT * FROM players WHERE uuid=?;"); PreparedStatement loadHomes =
                conn.prepareStatement("SELECT * FROM homes WHERE owner=?;")) {

            loadUser.setString(1, id.toString());
            ResultSet rs = loadUser.executeQuery();
            int level = 0;
            if (rs.next()) {
                level = rs.getInt("level");
            }

            loadHomes.setString(1, id.toString());
            rs = loadHomes.executeQuery();
            Set<Home> homes = new HashSet<>();
            while (rs.next()) {
                homes.add(constructHome(rs, id));
            }

            return new KingdomPlayer(id, new PlayerData(level, homes));
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return null;
         */
    }

    private List<Home> fetchHomes(Connection conn, UUID owner) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM homes WHERE owner=?;");
        ps.setString(1, owner.toString());

        List<Home> homes = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");

            UUID worldUUID = UUID.fromString(rs.getString("location_world"));
            double x = rs.getDouble("location_x");
            double y = rs.getDouble("location_y");
            double z = rs.getDouble("location_z");
            float yaw = rs.getFloat("location_yaw");
            float pitch = rs.getFloat("location_pitch");
            Location location = new Location(Bukkit.getWorld(worldUUID), x, y, z, yaw, pitch);

            homes.add(new Home(name, owner, location));
        }
        return homes;
    }

    @Override
    public void saveUser(KingdomPlayer player) {
        try (Connection conn = connProvider.getConnection(); PreparedStatement ps =
                conn.prepareStatement("MERGE INTO players KEY(uuid) VALUES(?);")) {

            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            sneakyThrow(e);
        }
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
}
