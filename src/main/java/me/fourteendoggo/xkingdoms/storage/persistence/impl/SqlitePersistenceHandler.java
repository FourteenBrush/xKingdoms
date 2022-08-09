package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Utils;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlitePersistenceHandler extends Database implements PersistenceHandler {

    public SqlitePersistenceHandler(XKingdoms plugin) {
        super(plugin, dataSource -> {
            Path dbPath = plugin.getDataFolder().toPath().toAbsolutePath().resolve("database");
            dataSource.setJdbcUrl("jdbc:sqlite:" + dbPath);
            dataSource.setDriverClassName("org.sqlite.JDBC");
        });
    }

    @Override
    public void connect() {
        try (Connection conn = getConnection()) {
            for (String query : Constants.SQLITE_INITIAL_TABLE_SETUP) {
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

            }
            return KingdomPlayer.newFirstJoinedPlayer(id);
        }, id);
    }

    @Override
    public void savePlayer(KingdomPlayer player) {

    }
}
