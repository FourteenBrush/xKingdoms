package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.Location;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class H2PersistenceHandler extends Database implements PersistenceHandler {

    public H2PersistenceHandler(XKingdoms plugin) {
        super();
        Path dbPath = plugin.getDataFolder().toPath().toAbsolutePath().resolve("database");
        dataSource.setJdbcUrl("jdbc:h2:file:" + dbPath + ";FILE_LOCK=FILE");
        dataSource.setDriverClassName("org.h2.Driver");
    }

    @Override
    public void connect() {
        try (Connection conn = getConnection()) {
            for (String query : Constants.H2_INITIAL_TABLE_SETUP) {
                executeRawQuery(conn, query);
            }
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
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

                PlayerData playerData = new PlayerData(level);
                homes.forEach(playerData::addHome);
                // TODO: load skills

                return new KingdomPlayer(id, playerData);
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
