package me.fourteendoggo.xkingdoms.database;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionProvider {
    private final HikariDataSource dataSource;
    private final StorageType type;

    public ConnectionProvider(Xkingdoms plugin) {
        String type = plugin.getConfig().getString("database.type");
        this.type = StorageType.fromString(type, StorageType.H2);
        this.dataSource = this.type.getDataSource(plugin);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public StorageType getType() {
        return type;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
