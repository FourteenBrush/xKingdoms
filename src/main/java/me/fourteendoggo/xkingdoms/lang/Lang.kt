package me.fourteendoggo.xkingdoms.lang

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.utils.Config
import me.fourteendoggo.xkingdoms.utils.Reloadable
import me.fourteendoggo.xkingdoms.utils.Utils
import org.apache.commons.lang.Validate
import java.util.logging.Logger

class Lang(plugin: XKingdoms) : Reloadable {
    private val config: Config
    private val logger: Logger
    private val cachedMessages = HashMap<String, String>()

    init {
        config = Config(plugin, "lang.yml", true)
        logger = plugin.logger
        fillMap()
        logger.info("Loaded messages")
    }

    override fun reload() {
        config.reload()
        fillMap()
    }

    private fun fillMap() {
        var saveRequired = false
        for (key in LangKey.entries) {
            val path = key.path
            var message = config.getString(path, null) // do not use defaults
            if (message == null) {
                val defaults = config.defaults
                Validate.notNull(defaults)
                message = defaults!!.getString(path)
                config[path] = message
                saveRequired = true
            }
            cachedMessages[path] = Utils.colorizeWithHex(message)
        }
        if (saveRequired) {
            logger.info("Some messages were not present in the lang.yml file and were replaced by default ones")
            config.save()
        }
    }

    fun getMessage(key: LangKey) = cachedMessages[key.path]!! // already colored

    fun getMessage(key: LangKey, vararg placeholders: Any?): String {
        return String.format(getMessage(key), *placeholders)
    }
}
