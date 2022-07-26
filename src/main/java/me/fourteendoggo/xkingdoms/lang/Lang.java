package me.fourteendoggo.xkingdoms.lang;

import me.fourteendoggo.xkingdoms.XKingdoms;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F\\d]{6}");
    private final Config config;
    private final Map<String, String> cachedMessages;

    public Lang(XKingdoms plugin) {
        this.config = new Config(plugin, "lang.yml", true);
        this.cachedMessages = new HashMap<>();
        fillMap();
        plugin.getLogger().info("Loaded messages");
    }

    public void reloadConfig() {
        config.reload();
        fillMap();
    }

    private void fillMap() {
        for (LangKey key : LangKey.values()) {
            String path = key.getPath();
            String message = config.getString(path);
            cachedMessages.put(path, colorize(message));
        }
    }

    private String colorize(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        while (matcher.find()) {
            String hexColor = input.substring(matcher.start(), matcher.end());
            input = input.replace(hexColor, ChatColor.of(hexColor).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public String getMessage(LangKey key) {
        return cachedMessages.get(key.getPath()); // already colored
    }

    public String getMessage(LangKey key, String... placeholders) {
        return getMessage(key).formatted((Object[]) placeholders); // tf java
    }
}
