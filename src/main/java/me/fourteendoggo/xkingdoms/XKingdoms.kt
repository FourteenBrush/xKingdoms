package me.fourteendoggo.xkingdoms

import co.aikar.commands.BukkitCommandManager
import me.fourteendoggo.xkingdoms.commands.HomeCommand
import me.fourteendoggo.xkingdoms.commands.ItemLockCommand
import me.fourteendoggo.xkingdoms.commands.ReloadCommand
import me.fourteendoggo.xkingdoms.commands.SkillCommand
import me.fourteendoggo.xkingdoms.commands.vanish.NewVanishCommand
import me.fourteendoggo.xkingdoms.commands.vanish.VanishManager
import me.fourteendoggo.xkingdoms.inventory.InventoryManager
import me.fourteendoggo.xkingdoms.lang.Lang
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.listeners.PlayerListener
import me.fourteendoggo.xkingdoms.listeners.SkillListener
import me.fourteendoggo.xkingdoms.player.KingdomPlayer
import me.fourteendoggo.xkingdoms.skill.SkillsManager
import me.fourteendoggo.xkingdoms.storage.management.UserManager
import me.fourteendoggo.xkingdoms.storage.persistence.Storage
import me.fourteendoggo.xkingdoms.storage.persistence.StorageType
import me.fourteendoggo.xkingdoms.utils.Home
import me.fourteendoggo.xkingdoms.utils.Reloadable
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class XKingdoms : JavaPlugin() {
    private var reloading = false
    private lateinit var lang: Lang
    lateinit var storage: Storage private set
    lateinit var userManager: UserManager private set
    private lateinit var vanishManager: VanishManager
    lateinit var inventoryManager: InventoryManager private set
    private lateinit var reloadableComponents: List<Reloadable>

    override fun onLoad() {
        reloading = Bukkit.getWorlds().isNotEmpty()
    }

    override fun onEnable() {
        saveDefaultConfig()

        val storageType = StorageType.parse(this, StorageType.JSON)
        val persistenceHandler = storageType.getPersistenceHandler(this)
        storage = Storage(persistenceHandler, logger)

        if (!storage.connect()) {
            // storage will have logged the error, we just need to shut down
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        logger.info("Using a ${storageType.description}")

        userManager = UserManager(storage)
        lang = Lang(this)
        vanishManager = VanishManager(this)
        inventoryManager = InventoryManager(this)

        reloadableComponents = listOf(lang, vanishManager)

        val commandManager = BukkitCommandManager(this)
        @Suppress("DEPRECATION")
        commandManager.enableUnstableAPI("help")

        commandManager.commandContexts.registerContext(Home::class.java) {
            val homeName = it.popFirstArg()
            val kPlayer = userManager.getUser(it.player.uniqueId)
            kPlayer!!.data.removeHome(homeName)
        }

        commandManager.commandCompletions.registerCompletion("@homes") {
            val kPlayer = userManager.getUser(it.player.uniqueId)
            kPlayer!!.data.homes.keys
        }

        commandManager.commandContexts.registerContext(KingdomPlayer::class.java) {
            userManager.getUser(it.player.uniqueId)
        }

        commandManager.registerCommand(HomeCommand(this))
        commandManager.registerCommand(ItemLockCommand(this))
        commandManager.registerCommand(ReloadCommand(this))
        commandManager.registerCommand(SkillCommand())
        commandManager.registerCommand(NewVanishCommand(this, vanishManager))

        Bukkit.getPluginManager().apply {
            registerEvents(PlayerListener(this@XKingdoms, vanishManager), this@XKingdoms)
            registerEvents(SkillListener(this@XKingdoms, SkillsManager()), this@XKingdoms)
        }

        if (reloading && Bukkit.getOnlinePlayers().isNotEmpty()) {
            logger.info("Reload detected, reloading players...")
            // renew states in case of the plugin jar being replaced
            for (player in Bukkit.getOnlinePlayers()) {
                userManager.loadIfAbsent(player.uniqueId)
                vanishManager.vanishIfFlagged(player)
            }
        }

        logger.info("$name has been enabled")
    }

    override fun onDisable() {
        vanishManager.destroyState()
        userManager.saveAllBlocking()
        storage.disconnect()
        logger.info("$name has been disabled")
    }

    fun reload() {
        reloadConfig()
        reloadableComponents.forEach(Reloadable::reload)
        logger.info("$name has been reloaded")
    }

    fun getLang(key: LangKey): String = lang.getMessage(key)

    fun getLang(key: LangKey, vararg placeholders: Any): String = lang.getMessage(key, placeholders)
}