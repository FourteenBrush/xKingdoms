package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Lang {
    private final FileConfiguration config;

    public Lang(XKingdoms plugin) {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        @SuppressWarnings("ConstantConditions")
        Reader defaultConfigStream = new InputStreamReader(plugin.getResource("lang.yml"), StandardCharsets.UTF_8);
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defaultConfigStream);

        this.config.setDefaults(defaults);
        this.config.options().copyDefaults(true);

        plugin.getLogger().info("Messages loaded, using plugins defaults as fallback.");
    }

    @SuppressWarnings("ConstantConditions") // values won't be null as they will be loaded from the default config
    public String get(LangKey key) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(key.getPath()));
    }

    public String get(LangKey key, String... placeholders) {
        return get(key).formatted((Object[]) placeholders);
    }
}
