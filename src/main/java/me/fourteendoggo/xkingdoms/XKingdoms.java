package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.*;
import me.fourteendoggo.xkingdoms.lang.Lang;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.listeners.SkillListener;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.SkillsManager;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.Storage;
import me.fourteendoggo.xkingdoms.storage.persistence.StorageType;
import me.fourteendoggo.xkingdoms.storage.repository.impl.KingdomCache;
import me.fourteendoggo.xkingdoms.storage.repository.impl.UserCache;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class XKingdoms extends JavaPlugin {
    private Lang lang;
    private Storage storage;
    private UserCache userCache;
    private KingdomCache kingdomCache;
    private Set<Reloadable> reloadableComponents;
    private SkillsManager skillsManager;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        saveDefaultConfig();

        StorageType storageType = StorageType.parse(this);
        PersistenceHandler persistenceHandler = storageType.getPersistenceHandler(this);
        storage = new Storage(persistenceHandler, getLogger());

        if (!storage.connect()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Using a " + storageType.getDescription());

        lang = new Lang(this);
        userCache = new UserCache(this);
        kingdomCache = new KingdomCache();
        reloadableComponents = new HashSet<>();

        reloadableComponents.add(lang);
        skillsManager = new SkillsManager();

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new HomeCommand(this));
        manager.registerCommand(new VanishCommand(this));
        manager.registerCommand(new ReloadCommand(this));
        manager.registerCommand(new SkillCommand());
        manager.registerCommand(new ServerStatsCommand());

        manager.getCommandCompletions().registerCompletion("@homes", context -> {
            // TODO this is null for some reason
            KingdomPlayer kPlayer = userCache.get(context.getPlayer().getUniqueId());
            return kPlayer.getData().getHomes().keySet();
        });

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new SkillListener(this), this);

        getLogger().info("xKingdoms has been enabled");
    }

    @Override
    public void onDisable() {
        if (!userCache.isEmpty()) {
            getLogger().info("Saving players to database...");
            userCache.forEach(storage::savePlayerSync);
        }
        storage.disconnect();

        getLogger().info(getName() + " has been disabled");
    }

    public void reloadAllComponents() {
        reloadConfig();
        reloadableComponents.forEach(Reloadable::reload);
        getLogger().info("Reloaded xKingdoms");
    }

    public String getLang(LangKey key) {
        return lang.getMessage(key);
    }

    public String getLang(LangKey key, String... placeholders) {
        return lang.getMessage(key, placeholders);
    }

    public Storage getStorage() {
        return storage;
    }

    public UserCache getUserCache() {
        return userCache;
    }

    public KingdomCache getKingdomCache() {
        return kingdomCache;
    }

    public SkillsManager getSkillsManager() {
        return skillsManager;
    }
}
