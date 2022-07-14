package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.HomeCommand;
import me.fourteendoggo.xkingdoms.commands.VanishCommand;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.database.DatabaseWrapper;
import me.fourteendoggo.xkingdoms.storage.database.StorageType;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.storage.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.storage.repository.KingdomRepository;
import me.fourteendoggo.xkingdoms.storage.repository.UserRepository;
import me.fourteendoggo.xkingdoms.utils.Lang;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import org.bukkit.plugin.java.JavaPlugin;

public class XKingdoms extends JavaPlugin {
    private Lang lang;
    private DatabaseWrapper database;
    private UserRepository userRepository;
    private KingdomRepository kingdomRepository;

    @Override
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public void onEnable() {
        saveDefaultConfig();

        StorageType storageType = StorageType.fromString(getConfig().getString("database.type"));
        ConnectionProvider connProvider = new ConnectionProvider(storageType.getDataSource(this));
        database = new DatabaseWrapper(storageType.getDatabase(connProvider), getLogger());

        if (database.connect()) {
            getLogger().info("Made a successful connection with a " + storageType.getDescription());
        } else { // an error occurred and the stacktrace was printed - stop, we can't work without database
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        lang = new Lang(this);
        userRepository = new UserRepository(this);
        kingdomRepository = new KingdomRepository();

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new HomeCommand(this));
        manager.registerCommand(new VanishCommand(this));

        manager.getCommandCompletions().registerCompletion("@homes", context -> {
            // TODO this is null for some reason
            KingdomPlayer kPlayer = userRepository.get(context.getPlayer().getUniqueId());
            return kPlayer.getData().getHomes().keySet();
        });

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("xKingdoms has been enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving players to database...");
        for (KingdomPlayer player : userRepository) {
            database.savePlayer(player).join(); // blocking to wait before shutting down
        }
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

    public KingdomRepository getKingdomRepository() {
        return kingdomRepository;
    }
}
