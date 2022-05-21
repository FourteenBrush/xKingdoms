package me.fourteendoggo.xkingdoms.storage;

import me.fourteendoggo.xkingdoms.Xkingdoms;

import javax.sql.DataSource;

public class ConnectionFactory {
    private final DataSource dataSource = null;
    private final StorageType type;

    public ConnectionFactory(Xkingdoms plugin) {
        String typeString = plugin.getConfig().getString("database.type");
        this.type = StorageType.fromString(typeString, StorageType.H2);
    }
}
