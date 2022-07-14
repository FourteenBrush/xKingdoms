package me.fourteendoggo.xkingdoms.storage.database;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.database.impl.H2Database;
import org.bukkit.configuration.file.FileConfiguration;

import java.nio.file.Path;

public abstract class StorageType {
    private final String description;

    public StorageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    protected HikariDataSource applySharedStuff(HikariDataSource dataSource, XKingdoms plugin) {
        dataSource.setMaximumPoolSize(plugin.getConfig().getInt("database.max-connections", 8));
        dataSource.setPoolName("[" + plugin.getName() + " - database]");
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.addDataSourceProperty("useSSL", false);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        return dataSource;
    }

    public abstract HikariDataSource getDataSource(XKingdoms plugin);

    public abstract Database getDatabase(ConnectionProvider provider);

    public static StorageType fromString(String str) {
        return str.equalsIgnoreCase("mysql") ? new MySQLStorageType() : new H2StorageType();
    }

    private static class H2StorageType extends StorageType {

        public H2StorageType() {
            super("H2 embedded database");
        }

        @Override
        public HikariDataSource getDataSource(XKingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
            Path dbFile = plugin.getDataFolder().toPath().toAbsolutePath().resolve("database");
            dataSource.setJdbcUrl("jdbc:h2:file:" + dbFile + ";FILE_LOCK=FILE");
            dataSource.setDriverClassName("org.h2.Driver");

            return applySharedStuff(dataSource, plugin);
        }

        @Override
        public Database getDatabase(ConnectionProvider provider) {
            return new H2Database(provider);
        }
    }

    private static class MySQLStorageType extends StorageType {

        public MySQLStorageType() {
            super("MySQL database");
        }

        @Override
        public HikariDataSource getDataSource(XKingdoms plugin) {
            HikariDataSource dataSource = new HikariDataSource();
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

            return dataSource;
        }

        @Override
        public Database getDatabase(ConnectionProvider provider) {
            return null; // TODO implement type
        }
    }
}
