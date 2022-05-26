package me.fourteendoggo.xkingdoms.database;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;

public enum StorageType {
    H2("H2 database in embedded mode", new H2DataSourceSupplier()),
    MYSQL("MySQL database", new MySQLDataSourceSupplier());

    private final String description;
    private final DataSourceSupplier dataSourceSupplier;

    StorageType(String description, DataSourceSupplier dataSourceSupplier) {
        this.description = description;
        this.dataSourceSupplier = dataSourceSupplier;
    }

    public String getDescription() {
        return description;
    }

    public HikariDataSource getDataSource(Xkingdoms plugin) {
        HikariDataSource dataSource = dataSourceSupplier.get(plugin);
        return applyCommonStuff(plugin, dataSource);
    }

    public static StorageType fromString(String str, StorageType def) {
        for (StorageType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return def;
    }

    private static HikariDataSource applyCommonStuff(Xkingdoms plugin, HikariDataSource dataSource) {
        dataSource.setMaximumPoolSize(plugin.getConfig().getInt("database.max-connections", 8));
        dataSource.setPoolName("[" + plugin.getName() + " - database]");
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        return dataSource;
    }

    @FunctionalInterface
    private interface DataSourceSupplier {

        HikariDataSource get(Xkingdoms plugin);
    }

    private static class H2DataSourceSupplier implements DataSourceSupplier {

        @Override
        public HikariDataSource get(Xkingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:h2:file:" + plugin.getDataFolder().toPath().toAbsolutePath().resolve("database"));
            dataSource.setDriverClassName("org.h2.Driver");
            return dataSource;
        }
    }

    private static class MySQLDataSourceSupplier implements DataSourceSupplier {

        @Override
        public HikariDataSource get(Xkingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            String address = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String dbName = plugin.getConfig().getString("database.name", "xkingdoms");
            String username = plugin.getConfig().getString("database.user", "mc");
            String password = plugin.getConfig().getString("database.password", "p@ssword");

            dataSource.setJdbcUrl("jdbc:mysql://%s:%s/%s".formatted(address, port, dbName));
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            return dataSource;
        }
    }
}
