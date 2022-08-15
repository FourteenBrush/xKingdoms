package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.*;
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
import me.fourteendoggo.xkingdoms.storage.repository.impl.KingdomCache;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class XKingdoms extends JavaPlugin {
    private boolean reloading;
    private Lang lang;
    private Storage storage;
    private UserManager userManager;
    private KingdomCache kingdomCache;
    private Set<Reloadable> reloadableComponents;
    private SkillsManager skillsManager;

    @Override
    public void onLoad() {
        reloading = !Bukkit.getWorlds().isEmpty();
    }

    @Override
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
        userManager = new UserManager(storage);
        kingdomCache = new KingdomCache();
        reloadableComponents = new HashSet<>();

        reloadableComponents.add(lang);
        skillsManager = new SkillsManager();

        registerCommands();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new SkillListener(this), this);

        if (reloading && !Bukkit.getOnlinePlayers().isEmpty() && isLoadingFromUpdateFolder()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                userManager.loadIfAbsent(player.getUniqueId());
            }
        }

        getLogger().info("xKingdoms has been enabled");
    }

    private boolean isLoadingFromUpdateFolder() {
        File updateFolder = Bukkit.getUpdateFolderFile();
        File[] contents = updateFolder.listFiles();
        if (contents == null || contents.length == 0) return false;

        for (File file : contents) {
            if (file.getName().endsWith(".jar") && file.getName().startsWith(getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        userManager.saveAll();
        storage.disconnect();
        getLogger().info("xKingdoms has been disabled");
    }

    public void reload() {
        reloadConfig();
        reloadableComponents.forEach(Reloadable::reload);
        getLogger().info("Reloaded xKingdoms");
    }

    @SuppressWarnings("deprecation")
    private void registerCommands() {
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new HomeCommand(this));
        manager.registerCommand(new VanishCommand(this));
        manager.registerCommand(new ReloadCommand(this));
        manager.registerCommand(new SkillCommand());
        manager.registerCommand(new ServerStatsCommand());

        manager.getCommandCompletions().registerCompletion("@homes", context -> {
            // TODO this is null for some reason
            KingdomPlayer kPlayer = userManager.getUser(context.getPlayer().getUniqueId());
            return kPlayer.getData().getHomes().keySet();
        });
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

    public UserManager getUserManager() {
        return userManager;
    }

    public KingdomCache getKingdomCache() {
        return kingdomCache;
    }

    public SkillsManager getSkillsManager() {
        return skillsManager;
    }
}
