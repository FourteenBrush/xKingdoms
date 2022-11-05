package me.fourteendoggo.xkingdoms.lang;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.utils.Config;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import me.fourteendoggo.xkingdoms.utils.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Lang implements Reloadable {
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

    private void fillMap() {
        boolean saveRequired = false;

        for (LangKey key : LangKey.values()) {
            String path = key.getPath();
            String message = config.getString(path, null); // do not use defaults

            if (message == null) {
                Configuration defaults = config.getDefaults();
                Validate.notNull(defaults);

                message = defaults.getString(path);
                config.set(path, message);
                saveRequired = true;
            }
            cachedMessages.put(path, Utils.colorizeWithHex(message));
        }
        if (saveRequired) {
            logger.info("Some messages were not present in the lang.yml file and were replaced by default ones");
            config.save();
        }
    }

    public String getMessage(LangKey key) {
        return cachedMessages.get(key.getPath()); // already colored
    }

    public String getMessage(LangKey key, Object... placeholders) {
        return getMessage(key).formatted(placeholders);
    }
}
