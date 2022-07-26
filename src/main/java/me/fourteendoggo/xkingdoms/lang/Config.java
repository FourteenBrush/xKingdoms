package me.fourteendoggo.xkingdoms.lang;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Config implements Reloadable {
    private final XKingdoms plugin;
    private final File configFile;
    private FileConfiguration config;
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
            this.config.setDefaults(YamlConfiguration.loadConfiguration(defaultConfigStream));
            this.config.options().copyDefaults(true);
        }
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save file " + configFile.getName(), e);
        }
    }

    @Override
    public void reload() {
        if (saveRequired) {
            save();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void set(String path, Object value) {
        config.set(path, value);
        saveRequired = true;
    }

    public void setAndSave(String path, Object value) {
        setAndSave(path, value, false);
    }

    public void setAndSave(String path, Object value, boolean saveAsync) {
        config.set(path, value);
        if (saveAsync) { // TODO saving in synchronized block?
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
        } else {
            save();
        }
        saveRequired = false;
    }

    public Set<String> getKeys(boolean deep) {
        return config.getKeys(deep);
    }

    public Map<String, Object> getValues(boolean deep) {
        return config.getValues(deep);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return config.getConfigurationSection(path);
    }
}
