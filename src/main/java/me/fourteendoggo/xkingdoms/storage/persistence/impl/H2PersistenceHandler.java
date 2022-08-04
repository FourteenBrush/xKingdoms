package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.skill.SkillData;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.bukkit.Location;
import org.intellij.lang.annotations.Language;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class H2PersistenceHandler extends Database implements PersistenceHandler {
    private static final @Language("SQL") String[] INITIAL_TABLE_SETUP = new String[] {
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid UUID PRIMARY KEY,
                level INT NOT NULL DEFAULT 0
            );""",
            """
            CREATE TABLE IF NOT EXISTS homes (
                id UUID PRIMARY KEY,
                owner UUID NOT NULL,
                name VARCHAR(40) NOT NULL,
                world UUID NOT NULL,
                x DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL
            );"""
    };
    private final HikariDataSource dataSource;

    public H2PersistenceHandler(XKingdoms plugin) {
        this.dataSource = new HikariDataSource();

        Path dbFile = plugin.getDataFolder().toPath().toAbsolutePath().resolve("database");
        dataSource.setJdbcUrl("jdbc:h2:file:" + dbFile + ";FILE_LOCK=FILE");
        dataSource.setDriverClassName("org.h2.Driver");
        applyDataSourceSettings(dataSource, plugin);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() {
        try (Connection conn = dataSource.getConnection()) {
            for (String query : INITIAL_TABLE_SETUP) {
                executeRawQuery(conn, query);
            }
        } catch (SQLException e) {
            sneakyThrow(e);
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return withConnection("SELECT * FROM players WHERE uuid=?;", (conn, ps) -> {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("level");
                List<Home> homes = fetchHomes(conn, id);
                SkillData skillData = new SkillData(); // TODO: implement proper loading

                return new KingdomPlayer(id, new PlayerData(level, skillData, homes));
            }
            return KingdomPlayer.newFirstJoinedPlayer(id);
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
    public void savePlayer(KingdomPlayer player) {
        withConnection("MERGE INTO players KEY(uuid, level) VALUES(?,?);", (conn, ps) -> {
            ps.executeUpdate();
            saveHomes(conn, player.getData().getHomes().values());
        }, player.getUniqueId(), player.getData().getLevel());
    }

    private void saveHomes(Connection conn, Collection<Home> homes) {
        String sql = "INSERT INTO homes(id, owner, name, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?,?);";
        withConnection(sql, conn, ps -> {
            int count = 0;
            for (Home home : homes) {
                Location loc = home.location();
                assert loc.getWorld() != null;

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, home.owner().toString());
                ps.setString(3, home.name());
                ps.setString(4, loc.getWorld().getUID().toString());
                ps.setInt(5, loc.getBlockX());
                ps.setInt(6, loc.getBlockY());
                ps.setInt(7, loc.getBlockZ());
                ps.setFloat(8, loc.getYaw());
                ps.setFloat(9, loc.getPitch());

                ps.addBatch();
                count++;

                // execute every 10 records or fewer
                if (count % 10 == 0 || count == homes.size()) {
                    ps.executeBatch();
                }
            }
        });
    }
}
