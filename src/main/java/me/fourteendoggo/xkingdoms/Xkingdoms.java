package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.SethomeCommand;
import me.fourteendoggo.xkingdoms.database.Database;
import me.fourteendoggo.xkingdoms.database.DatabaseWrapper;
import me.fourteendoggo.xkingdoms.database.impl.H2Database;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.utils.Lang;
import org.bukkit.plugin.java.JavaPlugin;

public class Xkingdoms extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new SethomeCommand());

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        new Lang(this);

        ConnectionProvider connProvider = new ConnectionProvider(this);
        DatabaseWrapper database = new DatabaseWrapper(new H2Database(connProvider), getLogger());
        if (database.connect()) {
            getLogger().info("Database connection was successful.");
        } else {
            getLogger().severe("Failed to connect to the database, did you provide the right credentials?");
            getServer().getPluginManager().disablePlugin(this);
        }

        getLogger().info("xKingdoms has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("xKingdoms has been disabled.");
    }
}
