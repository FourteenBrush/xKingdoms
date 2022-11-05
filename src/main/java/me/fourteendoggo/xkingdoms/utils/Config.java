package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Config implements Reloadable {
    private final XKingdoms plugin;
    private final String fileName;
    private final boolean copyDefaults;
    private final File file;
    private FileConfiguration configuration;
    private boolean saveRequired;

    public Config(XKingdoms plugin, String fileName, boolean copyDefaults) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.copyDefaults = copyDefaults;
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        reload(); // saveRequired is false so we are fine
    }

    @Override
    public void reload() {
        if (saveRequired) {
            save();
        }
        configuration = YamlConfiguration.loadConfiguration(file);

        if (copyDefaults) {
            InputStream defaults = plugin.getResource(fileName);
            if (defaults == null) {
                plugin.getLogger().warning("Cannot load the defaults for a config file because the defaults don't exist");
                return;
            }

            Reader reader = new InputStreamReader(defaults, StandardCharsets.UTF_8);
            configuration.setDefaults(YamlConfiguration.loadConfiguration(reader));
            configuration.options().copyDefaults(true);
        }
    }

    public void save() {
        try {
            configuration.save(file);
            saveRequired = false;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config file %s to disk".formatted(file.getPath()), e);
        }
    }

    public void setAndSave(String path, Object value, boolean saveAsync) {
        configuration.set(path, value);
        if (saveAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
        } else {
            save();
        }
    }

    public void addDefault(@NotNull String path, @Nullable Object value) {
        configuration.addDefault(path, value);
    }

    public void addDefaults(@NotNull Map<String, Object> defaults) {
        configuration.addDefaults(defaults);
    }

    public void addDefaults(@NotNull Configuration defaults) {
        configuration.addDefaults(defaults);
    }

    public void setDefaults(@NotNull Configuration defaults) {
        configuration.setDefaults(defaults);
    }

    @Nullable
    public Configuration getDefaults() {
        return configuration.getDefaults();
    }

    @Nullable
    public ConfigurationSection getParent() {
        return configuration.getParent();
    }

    @NotNull
    public Set<String> getKeys(boolean deep) {
        return configuration.getKeys(deep);
    }

    @NotNull
    public Map<String, Object> getValues(boolean deep) {
        return configuration.getValues(deep);
    }

    public boolean contains(@NotNull String path) {
        return configuration.contains(path);
    }

    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        return configuration.contains(path, ignoreDefault);
    }

    public boolean isSet(@NotNull String path) {
        return configuration.isSet(path);
    }

    @NotNull
    public String getCurrentPath() {
        return configuration.getCurrentPath();
    }

    @NotNull
    public String getName() {
        return configuration.getName();
    }

    @Nullable
    public Configuration getRoot() {
        return configuration.getRoot();
    }

    @Nullable
    public ConfigurationSection getDefaultSection() {
        return configuration.getDefaultSection();
    }

    public void set(@NotNull String path, @Nullable Object value) {
        configuration.set(path, value);
        saveRequired = true;
    }

    @Nullable
    public Object get(@NotNull String path) {
        return configuration.get(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Object get(@NotNull String path, @Nullable Object def) {
        return configuration.get(path, def);
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path) {
        return configuration.createSection(path);
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        return configuration.createSection(path, map);
    }

    @Nullable
    public String getString(@NotNull String path) {
        return configuration.getString(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public String getString(@NotNull String path, @Nullable String def) {
        return configuration.getString(path, def);
    }

    public boolean isString(@NotNull String path) {
        return configuration.isString(path);
    }

    public int getInt(@NotNull String path) {
        return configuration.getInt(path);
    }

    public int getInt(@NotNull String path, int def) {
        return configuration.getInt(path, def);
    }

    public boolean isInt(@NotNull String path) {
        return configuration.isInt(path);
    }

    public boolean getBoolean(@NotNull String path) {
        return configuration.getBoolean(path);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return configuration.getBoolean(path, def);
    }

    public boolean isBoolean(@NotNull String path) {
        return configuration.isBoolean(path);
    }

    public double getDouble(@NotNull String path) {
        return configuration.getDouble(path);
    }

    public double getDouble(@NotNull String path, double def) {
        return configuration.getDouble(path, def);
    }

    public boolean isDouble(@NotNull String path) {
        return configuration.isDouble(path);
    }

    public long getLong(@NotNull String path) {
        return configuration.getLong(path);
    }

    public long getLong(@NotNull String path, long def) {
        return configuration.getLong(path, def);
    }

    public boolean isLong(@NotNull String path) {
        return configuration.isLong(path);
    }

    @Nullable
    public List<?> getList(@NotNull String path) {
        return configuration.getList(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return configuration.getList(path, def);
    }

    public boolean isList(@NotNull String path) {
        return configuration.isList(path);
    }

    @NotNull
    public List<String> getStringList(@NotNull String path) {
        return configuration.getStringList(path);
    }

    @NotNull
    public List<Integer> getIntegerList(@NotNull String path) {
        return configuration.getIntegerList(path);
    }

    @NotNull
    public List<Boolean> getBooleanList(@NotNull String path) {
        return configuration.getBooleanList(path);
    }

    @NotNull
    public List<Double> getDoubleList(@NotNull String path) {
        return configuration.getDoubleList(path);
    }

    @NotNull
    public List<Float> getFloatList(@NotNull String path) {
        return configuration.getFloatList(path);
    }

    @NotNull
    public List<Long> getLongList(@NotNull String path) {
        return configuration.getLongList(path);
    }

    @NotNull
    public List<Byte> getByteList(@NotNull String path) {
        return configuration.getByteList(path);
    }

    @NotNull
    public List<Character> getCharacterList(@NotNull String path) {
        return configuration.getCharacterList(path);
    }

    @NotNull
    public List<Short> getShortList(@NotNull String path) {
        return configuration.getShortList(path);
    }

    @NotNull
    public List<Map<?, ?>> getMapList(@NotNull String path) {
        return configuration.getMapList(path);
    }

    @Nullable
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        return configuration.getObject(path, clazz);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return configuration.getObject(path, clazz, def);
    }

    @Nullable
    public <T extends ConfigurationSerializable> T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        return configuration.getSerializable(path, clazz);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    public <T extends ConfigurationSerializable> T getSerializable(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return configuration.getSerializable(path, clazz, def);
    }

    @Nullable
    public Vector getVector(@NotNull String path) {
        return configuration.getVector(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Vector getVector(@NotNull String path, @Nullable Vector def) {
        return configuration.getVector(path, def);
    }

    public boolean isVector(@NotNull String path) {
        return configuration.isVector(path);
    }

    @Nullable
    public OfflinePlayer getOfflinePlayer(@NotNull String path) {
        return configuration.getOfflinePlayer(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public OfflinePlayer getOfflinePlayer(@NotNull String path, @Nullable OfflinePlayer def) {
        return configuration.getOfflinePlayer(path, def);
    }

    public boolean isOfflinePlayer(@NotNull String path) {
        return configuration.isOfflinePlayer(path);
    }

    @Nullable
    public ItemStack getItemStack(@NotNull String path) {
        return configuration.getItemStack(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return configuration.getItemStack(path, def);
    }

    public boolean isItemStack(@NotNull String path) {
        return configuration.isItemStack(path);
    }

    @Nullable
    public Color getColor(@NotNull String path) {
        return configuration.getColor(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Color getColor(@NotNull String path, @Nullable Color def) {
        return configuration.getColor(path, def);
    }

    public boolean isColor(@NotNull String path) {
        return configuration.isColor(path);
    }

    @Nullable
    public Location getLocation(@NotNull String path) {
        return configuration.getLocation(path);
    }

    @Nullable
    @Contract("_, !null -> !null")
    public Location getLocation(@NotNull String path, @Nullable Location def) {
        return configuration.getLocation(path, def);
    }

    public boolean isLocation(@NotNull String path) {
        return configuration.isLocation(path);
    }

    @Nullable
    public ConfigurationSection getConfigurationSection(@NotNull String path) {
        return configuration.getConfigurationSection(path);
    }

    public boolean isConfigurationSection(@NotNull String path) {
        return configuration.isConfigurationSection(path);
    }
}
