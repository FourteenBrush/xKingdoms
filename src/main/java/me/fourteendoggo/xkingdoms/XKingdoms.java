package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.HomeCommand;
import me.fourteendoggo.xkingdoms.commands.ReloadCommand;
import me.fourteendoggo.xkingdoms.commands.VanishCommand;
import me.fourteendoggo.xkingdoms.lang.Lang;
import me.fourteendoggo.xkingdoms.listeners.SkillListener;
import me.fourteendoggo.xkingdoms.storage.persistance.PersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistance.Storage;
import me.fourteendoggo.xkingdoms.storage.persistance.StorageType;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.SkillsManager;
import me.fourteendoggo.xkingdoms.listeners.PlayerListener;
import me.fourteendoggo.xkingdoms.storage.repository.impl.KingdomRepository;
import me.fourteendoggo.xkingdoms.storage.repository.impl.UserRepository;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class XKingdoms extends JavaPlugin {
    private Lang lang;
    private Storage storage;
    private UserRepository userRepository;
    private KingdomRepository kingdomRepository;
    private Set<Reloadable> reloadableComponents;
    private SkillsManager skillsManager;

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        saveDefaultConfig();

        StorageType storageType = StorageType.parse(this);
        PersistenceHandler persistenceHandler = storageType.getPersistenceHandler();
        storage = new Storage(persistenceHandler, getLogger());

        if (!storage.connect()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // database.executePatches(); TODO: this will be implemented in the database::connect method
        getLogger().info("Made a successful connection with a " + storageType.getDescription());

        lang = new Lang(this);
        userRepository = new UserRepository(this);
        kingdomRepository = new KingdomRepository();
        reloadableComponents = new HashSet<>();

        reloadableComponents.add(lang);
        skillsManager = new SkillsManager();

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new HomeCommand(this));
        manager.registerCommand(new VanishCommand(this));
        manager.registerCommand(new ReloadCommand(this));

        manager.getCommandCompletions().registerCompletion("@homes", context -> {
            // TODO this is null for some reason
            KingdomPlayer kPlayer = userRepository.get(context.getPlayer().getUniqueId());
            return kPlayer.getData().getHomes().keySet();
        });

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new SkillListener(this), this);

        getLogger().info("xKingdoms has been enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving players to database...");
        userRepository.forEach(storage::savePlayerSync);
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

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public KingdomRepository getKingdomRepository() {
        return kingdomRepository;
    }

    public SkillsManager getSkillsManager() {
        return skillsManager;
    }
}
