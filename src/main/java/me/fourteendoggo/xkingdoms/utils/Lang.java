package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.Xkingdoms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Lang {
    private final FileConfiguration config;

    public Lang(Xkingdoms plugin) {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.config.options().copyDefaults(true);

        plugin.getLogger().info("Language file loaded, using defaults as fallback.");
    }

    public String get(LangKey key) {
        return this.config.getString(key.getPath());
    }

    public String get(LangKey key, String... placeholders) {
        return get(key).formatted((Object[]) placeholders);
    }
}
