package me.fourteendoggo.xkingdoms.lang;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.utils.Config;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang implements Reloadable {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F\\d]{6}");
    private final Config config;
    private final Logger logger;
    private final Map<String, String> cachedMessages;

    public Lang(XKingdoms plugin) {
        this.config = new Config(plugin, "lang.yml", true);
        this.logger = plugin.getLogger();
        this.cachedMessages = new HashMap<>();
        fillMap();
        logger.info("Loaded messages");
    }

    @Override
    public void reload() {
        config.reload();
        fillMap();
    }

    @SuppressWarnings("ConstantConditions")
    private void fillMap() {
        boolean needsSave = false;
        for (LangKey key : LangKey.values()) {
            String path = key.getPath();
            String message;
            if (config.isSet(path)) { // TODO: config.get(path, null) != null?
                message = config.getString(path);
            } else {
                message = config.getDefaults().getString(path);
                config.set(path, message);
                needsSave = true;

                logger.warning("==========");
                logger.warning("A message was not present in the lang.yml file, replacing it...");
            }
            cachedMessages.put(path, colorize(message));
        }
        if (needsSave) {
            config.reload();
        }
    }

    // TODO MessageFormat?
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
        return getMessage(key).formatted((Object[]) placeholders); // tf java how am I supposed to know
    }
}
