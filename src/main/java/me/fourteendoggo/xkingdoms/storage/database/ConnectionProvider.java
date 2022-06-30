package me.fourteendoggo.xkingdoms.storage.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionProvider {
    private final HikariDataSource dataSource;
    private final StorageType type;

    public ConnectionProvider(HikariDataSource dataSource, StorageType type) {
        this.dataSource = dataSource;
        this.type = type;
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
}
