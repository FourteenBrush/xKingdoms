package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.HomeCommand;
import me.fourteendoggo.xkingdoms.storage.database.DatabaseWrapper;
import me.fourteendoggo.xkingdoms.storage.database.StorageType;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.storage.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.storage.repository.UserRepository;
import me.fourteendoggo.xkingdoms.utils.Lang;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import org.bukkit.plugin.java.JavaPlugin;

public class XKingdoms extends JavaPlugin {
    private Lang lang;
    private DatabaseWrapper database;
    private UserRepository userRepository;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        StorageType storageType = StorageType.fromString(getConfig().getString("database.type"));
        ConnectionProvider connProvider = new ConnectionProvider(storageType.getDataSource(this), storageType);
        database = new DatabaseWrapper(storageType.getDatabase(connProvider), getLogger());

        if (database.connect()) {
            getLogger().info("Connection with %s was successful".formatted(storageType.getDescription()));
        } else { // an error occurred and the stacktrace was printed - abort, we can't work without database
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new HomeCommand(this));

        lang = new Lang(this);
        userRepository = new UserRepository(database::loadUser);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info(getName() + " has been enabled");
    }

    @Override
    public void onDisable() {
        database.disconnect();
        getLogger().info(getName() + " has been disabled");
    }

    public String getLang(LangKey key, String... placeholders) {
        return lang.get(key, placeholders);
    }

    public String getLang(LangKey key) {
        return lang.get(key);
    }

    public DatabaseWrapper getDatabase() {
        return database;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
