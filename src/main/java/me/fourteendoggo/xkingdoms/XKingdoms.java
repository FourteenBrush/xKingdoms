package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.SethomeCommand;
import me.fourteendoggo.xkingdoms.database.Database;
import me.fourteendoggo.xkingdoms.database.DatabaseWrapper;
import me.fourteendoggo.xkingdoms.database.StorageType;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.utils.Lang;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import org.bukkit.plugin.java.JavaPlugin;

public class XKingdoms extends JavaPlugin {

    private Lang lang;
    private DatabaseWrapper database;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        StorageType type = StorageType.fromString(getConfig().getString("database.type"));
        ConnectionProvider connProvider = new ConnectionProvider(type.getDataSource(this), type);
        database = new DatabaseWrapper(type.getDatabase(connProvider), getLogger());

        if (database.connect()) {
            getLogger().info("Connection with %s was successful".formatted(type.getDescription()));
        } else { // an error occurred and the stacktrace was printed - abort, we can't work without database
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new SethomeCommand(this));
        lang = new Lang(this);

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
}
