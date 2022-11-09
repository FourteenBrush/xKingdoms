package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.HomeCommand;
import me.fourteendoggo.xkingdoms.commands.ItemLockCommand;
import me.fourteendoggo.xkingdoms.commands.ReloadCommand;
import me.fourteendoggo.xkingdoms.commands.SkillCommand;
import me.fourteendoggo.xkingdoms.commands.vanish.NewVanishCommand;
import me.fourteendoggo.xkingdoms.commands.vanish.VanishManager;
import me.fourteendoggo.xkingdoms.inventory.InventoryManager;
import me.fourteendoggo.xkingdoms.lang.Lang;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.listeners.SkillListener;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.SkillsManager;
import me.fourteendoggo.xkingdoms.storage.management.UserManager;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.Storage;
import me.fourteendoggo.xkingdoms.storage.persistence.StorageType;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class XKingdoms extends JavaPlugin {
    private boolean reloading;
    private Lang lang;
    private Storage storage;
    private UserManager userManager;
    // A set of components that need to be reloaded when calling /xkingdoms reload
    private List<Reloadable> reloadableComponents;
    private VanishManager vanishManager;
    private InventoryManager inventoryManager;

    @Override
    public void onLoad() {
        reloading = !Bukkit.getWorlds().isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        saveDefaultConfig();

        StorageType storageType = StorageType.parse(this);
        PersistenceHandler persistenceHandler = storageType.getPersistenceHandler(this);
        storage = new Storage(persistenceHandler, getLogger());

        if (!storage.connect()) {
            // storage will have logged the error, we just need to shut down
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Using a " + storageType.getDescription());

        userManager = new UserManager(storage);
        lang = new Lang(this);
        vanishManager = new VanishManager(this);
        inventoryManager = new InventoryManager(this);

        reloadableComponents = Arrays.asList(
                lang, vanishManager
        );

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");

        commandManager.getCommandContexts().registerContext(Home.class, resolver -> {
            String homeName = resolver.popFirstArg();
            Player player = resolver.getPlayer();
            KingdomPlayer kingdomPlayer = userManager.getUser(player.getUniqueId());
            return kingdomPlayer.getData().removeHome(homeName);
        });

        commandManager.getCommandCompletions().registerCompletion("@homes", handler -> {
            KingdomPlayer kPlayer = userManager.getUser(handler.getPlayer().getUniqueId());
            return kPlayer.getData().getHomes().keySet();
        });
        commandManager.getCommandContexts().registerContext(KingdomPlayer.class, resolver ->
                userManager.getUser(resolver.getPlayer().getUniqueId()));

        commandManager.registerCommand(new HomeCommand(this));
        commandManager.registerCommand(new ItemLockCommand(this));
        commandManager.registerCommand(new ReloadCommand(this));
        commandManager.registerCommand(new SkillCommand());
        commandManager.registerCommand(new NewVanishCommand(this, vanishManager));

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this, vanishManager), this);
        pm.registerEvents(new SkillListener(this, new SkillsManager()), this);

        if (reloading && !Bukkit.getOnlinePlayers().isEmpty()) {
            getLogger().info("Reload detected, reloading players...");
            // renew states in case of the plugin jar being renewed
            for (Player player : Bukkit.getOnlinePlayers()) {
                userManager.loadIfAbsent(player.getUniqueId());
                vanishManager.vanishIfFlagged(player);
            }
        }

        getLogger().info("xKingdoms has been enabled");
    }

    @Override
    public void onDisable() {
        vanishManager.destroyState();
        userManager.saveAllBlocking();
        storage.disconnect();
        getLogger().info("xKingdoms has been disabled");
    }

    public void reload() {
        reloadConfig();
        reloadableComponents.forEach(Reloadable::reload);
        getLogger().info("xKingdoms has been reloaded");
    }

    public String getLang(LangKey key) {
        return lang.getMessage(key);
    }

    public String getLang(LangKey key, Object... placeholders) {
        return lang.getMessage(key, placeholders);
    }

    public Storage getStorage() {
        return storage;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
