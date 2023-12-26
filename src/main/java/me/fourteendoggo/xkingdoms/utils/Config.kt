package me.fourteendoggo.xkingdoms.utils

import me.fourteendoggo.xkingdoms.XKingdoms
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level

class Config(
        private val plugin: XKingdoms,
        private val fileName: String,
        private val copyDefaults: Boolean,
        // yes this is ugly but who cares, at least we aren't manually delegating anymore
        private var config: FileConfiguration = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, fileName))
) : Reloadable, Configuration by config {
    private val file = File(plugin.dataFolder, fileName)
    private var saveRequired = false

    init {
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        }
        reload() // saveRequired is false so we are fine
    }

    override fun reload() {
        if (saveRequired) {
            save()
        }
        config = YamlConfiguration.loadConfiguration(file)
        if (copyDefaults) {
            val defaults = plugin.getResource(fileName)
            if (defaults == null) {
                plugin.logger.warning("Cannot load the defaults for a config file because the defaults don't exist")
                return
            }
            val reader = InputStreamReader(defaults, StandardCharsets.UTF_8)
            config.setDefaults(YamlConfiguration.loadConfiguration(reader))
            config.options().copyDefaults(true)
        }
    }

    fun save() {
        try {
            config.save(file)
            saveRequired = false
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save config file ${file.path} to disk", e)
        }
    }

    fun setAndSave(path: String, value: Any?, saveAsync: Boolean) {
        config[path] = value
        when (saveAsync) {
            true -> Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save)
            false -> save()
        }
    }
}
