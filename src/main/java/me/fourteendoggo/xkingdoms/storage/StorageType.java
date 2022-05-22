package me.fourteendoggo.xkingdoms.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;

@SuppressWarnings("resource")
public enum StorageType {
    H2("H2 database in embedded mode", new H2DataSource()),
    MYSQL("MySQL database", new MySQLDataSource());

    private final String description;
    private final DataSourceSupplier dataSourceSupplier;

    StorageType(String description, DataSourceSupplier dataSourceSupplier) {
        this.description = description;
        this.dataSourceSupplier = dataSourceSupplier;
    }

    public String getDescription() {
        return this.description;
    }

    public HikariDataSource getDataSource(Xkingdoms plugin) {
        HikariDataSource dataSource = this.dataSourceSupplier.get(plugin);
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
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("[" + plugin.getName() + " - Database]");
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

    private static class H2DataSource implements DataSourceSupplier {

        @Override
        public HikariDataSource get(Xkingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:h2:file:" + plugin.getDataFolder().toPath().toAbsolutePath().resolve("database.h2.db"));
            dataSource.setDriverClassName("org.h2.Driver"); // TODO shade driver
            return applyCommonStuff(plugin, dataSource);
        }
    }

    private static class MySQLDataSource implements DataSourceSupplier {

        @Override
        public HikariDataSource get(Xkingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            String address = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String dbName = plugin.getConfig().getString("database.name", "minecraft");
            String username = plugin.getConfig().getString("database.user", "mc");
            String password = plugin.getConfig().getString("database.password", "p@ssword");

            dataSource.setJdbcUrl("jdbc:mysql://%s:%s/%s".formatted(address, port, dbName));
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            return applyCommonStuff(plugin, dataSource);
        }
    }
}
