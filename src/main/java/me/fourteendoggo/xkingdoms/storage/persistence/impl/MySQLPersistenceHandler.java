package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class MySQLPersistenceHandler extends Database implements PersistenceHandler { // TODO: implement methods

    public MySQLPersistenceHandler(XKingdoms plugin) {
        super();
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
