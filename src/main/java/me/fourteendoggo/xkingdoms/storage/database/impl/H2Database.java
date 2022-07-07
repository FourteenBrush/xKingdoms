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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class H2Database implements Database {
    private static final @Language("SQL") String[] TABLE_SETUP = new String[]{
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS homes (
                owner VARCHAR(36) PRIMARY KEY,
                name VARCHAR(40) NOT NULL,
                location_world VARCHAR(36) NOT NULL,
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

    @Override
    public KingdomPlayer loadUser(UUID id) {
        return new KingdomPlayer(id, new PlayerData(loadHomes(id)));
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

    public Set<Home> loadHomes(UUID owner) {
        Set<Home> homes = new HashSet<>();
        try (Connection conn = connProvider.getConnection(); PreparedStatement ps =
                conn.prepareStatement("SELECT * FROM homes WHERE owner=?;")) {

            ps.setString(1, owner.toString());
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
        } catch (SQLException e) {
            sneakyThrow(e);
        }
        return homes;
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }
}
