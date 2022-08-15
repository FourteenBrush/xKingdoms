package me.fourteendoggo.xkingdoms.lang;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.utils.Config;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import me.fourteendoggo.xkingdoms.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Lang implements Reloadable { // TODO: Map<String, MessageFormat>?
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
        boolean saveRequired = false;
        for (LangKey key : LangKey.values()) {
            String path = key.getPath();
            String message;

            if (config.isSet(path)) { // TODO: config.get(path, null) != null?
                message = config.getString(path);
            } else {
                message = config.getDefaults().getString(path);
                config.set(path, message);
                saveRequired = true;
            }
            cachedMessages.put(path, Utils.colorizeWithHexSupport(message));
        }
        if (saveRequired) {
            logger.info("Some messages were not present in the lang.yml file and were replaced");
            config.reload();
        }
    }

    public String getMessage(LangKey key) {
        return cachedMessages.get(key.getPath()); // already colored
    }

    public String getMessage(LangKey key, String... placeholders) {
        return getMessage(key).formatted((Object[]) placeholders); // tf java how am I supposed to know
    }
}
