package me.fourteendoggo.xkingdoms.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private final HikariDataSource dataSource;
    private final StorageType type;

    public ConnectionFactory(Xkingdoms plugin) {
        String type = plugin.getConfig().getString("database.type");
        this.type = StorageType.fromString(type, StorageType.H2);
        this.dataSource = this.type.getDataSource(plugin);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public StorageType getType() {
        return this.type;
    }

    public void close() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }
    }
}
