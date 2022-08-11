package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LazyConnection {
    private final String jdbcUrl;
    private Connection connection;

    public LazyConnection(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || !connection.isValid(100)) {
            connection = DriverManager.getConnection(jdbcUrl);
        }
        return connection;
    }
}
