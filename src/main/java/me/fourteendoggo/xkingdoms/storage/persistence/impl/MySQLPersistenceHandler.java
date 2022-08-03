package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLPersistenceHandler extends Database implements PersistenceHandler { // TODO: implement methods
    private final HikariDataSource dataSource;

    public MySQLPersistenceHandler(XKingdoms plugin) {
        this.dataSource = new HikariDataSource();

        String username = plugin.getConfig().getString("database.username", "mc");
        String password = plugin.getConfig().getString("database.password", "p@ssword");

        FileConfiguration config = plugin.getConfig();
        dataSource.setJdbcUrl("jdbc:mysql://%s:%s/%s".formatted(
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.name", plugin.getName())
        ));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        applyDataSourceSettings(dataSource, plugin);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return null;
    }

    @Override
    public void savePlayer(KingdomPlayer player) {

    }
}
