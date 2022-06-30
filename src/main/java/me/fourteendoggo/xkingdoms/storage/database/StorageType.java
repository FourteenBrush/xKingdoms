package me.fourteendoggo.xkingdoms.storage.database;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.database.impl.H2Database;

import java.util.function.Function;

public enum StorageType {
    H2("H2 database in embedded mode", new H2DataSourceSupplier(), H2Database::new),
    MYSQL("MySQL database", new MySQLDataSourceSupplier(), null); // TODO

    private final String description;
    private final Function<XKingdoms, HikariDataSource> dataSourceFunction;
    private final Function<ConnectionProvider, Database> databaseFunction;

    StorageType(String description,
                Function<XKingdoms, HikariDataSource> dataSourceFunction,
                Function<ConnectionProvider, Database> databaseFunction) {
        this.description = description;
        this.dataSourceFunction = dataSourceFunction;
        this.databaseFunction = databaseFunction;
    }

    public String getDescription() {
        return description;
    }

    public HikariDataSource getDataSource(XKingdoms plugin) {
        HikariDataSource dataSource = dataSourceFunction.apply(plugin);
        return applyCommonStuff(plugin, dataSource);
    }

    public Database getDatabase(ConnectionProvider connProvider) {
        return databaseFunction.apply(connProvider);
    }

    public static StorageType fromString(String str) {
        for (StorageType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return H2;
    }

    private static HikariDataSource applyCommonStuff(XKingdoms plugin, HikariDataSource dataSource) {
        dataSource.setMaximumPoolSize(plugin.getConfig().getInt("database.max-connections", 8));
        dataSource.setPoolName("[" + plugin.getName() + " - database]");
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        return dataSource;
    }

    private static class H2DataSourceSupplier implements Function<XKingdoms, HikariDataSource> {

        @Override
        public HikariDataSource apply(XKingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:h2:file:" + plugin.getDataFolder().toPath().toAbsolutePath().resolve("database"));
            dataSource.setDriverClassName("org.h2.Driver");
            return dataSource;
        }
    }

    private static class MySQLDataSourceSupplier implements Function<XKingdoms, HikariDataSource> {

        @Override
        public HikariDataSource apply(XKingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            String username = plugin.getConfig().getString("database.username", "mc");
            String password = plugin.getConfig().getString("database.password", "p@ssword");

            dataSource.setJdbcUrl("jdbc:mysql://%s:%s/%s".formatted(
                    plugin.getConfig().getString("database.host", "localhost"),
                    plugin.getConfig().getInt("database.port", 3306),
                    plugin.getConfig().getString("database.name", plugin.getName())
            ));
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            return dataSource;
        }
    }
}
