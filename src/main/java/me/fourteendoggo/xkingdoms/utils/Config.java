package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Config extends YamlConfiguration implements Reloadable {
    private final XKingdoms plugin;
    private final File configFile;
    private boolean saveRequired;

    public Config(XKingdoms plugin, String filename) {
        this(plugin, filename, false);
    }

    public Config(XKingdoms plugin, String filename, boolean copyDefaults) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), filename);
        if (!configFile.exists()) {
            plugin.saveResource(filename, false);
        }
        reload();
        if (copyDefaults) {
            @SuppressWarnings("ConstantConditions")
            Reader defaultConfigStream = new InputStreamReader(plugin.getResource(filename), StandardCharsets.UTF_8);
            setDefaults(YamlConfiguration.loadConfiguration(defaultConfigStream));
            options().copyDefaults(true);
        }
    }

    public void save() {
        try {
            save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save file " + configFile.getName(), e);
        }
    }

    @Override
    public void reload() {
        if (saveRequired) {
            save();
        }
        loadConfiguration(configFile);
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        super.set(path, value);
        saveRequired = true;
    }

    public void setAndSave(String path, Object value) {
        setAndSave(path, value, false);
    }

    public void setAndSave(String path, Object value, boolean saveAsync) {
        set(path, value);
        if (saveAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> save());
        } else {
            save();
        }
        saveRequired = false;
    }
}
