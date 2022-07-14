package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
public class Lang {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F\\d]{6}");
    private final FileConfiguration config;

    public Lang(XKingdoms plugin) {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        Reader defaultConfigStream = new InputStreamReader(plugin.getResource("lang.yml"), StandardCharsets.UTF_8);
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defaultConfigStream);

        this.config.setDefaults(defaults);
        this.config.options().copyDefaults(true);

        plugin.getLogger().info("Messages loaded, using plugins defaults as fallback.");
    }

    public String get(LangKey key) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(key.getPath()));
    }

    public String get(LangKey key, String... placeholders) {
        return get(key).formatted((Object[]) placeholders);
    }

    private String colorizeHex(String input) {
        throw new UnsupportedOperationException(); // TODO
    }
}
