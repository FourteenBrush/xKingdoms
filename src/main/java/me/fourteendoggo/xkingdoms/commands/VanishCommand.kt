package me.fourteendoggo.xkingdoms.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.utils.Constants
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

@CommandAlias("vanish|v")
@CommandPermission("xkingdoms.moderator")
@Description("Main command for vanishing")
class VanishCommand(private val plugin: XKingdoms) : BaseCommand(), Listener {
    private val vanished = HashSet<UUID>()
    private val bossbar = Bukkit.createBossBar("Vanished", BarColor.BLUE, BarStyle.SOLID)
    private val key: NamespacedKey = NamespacedKey(plugin, "vanished")

    init {
        bossbar.removeAll()
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @Default
    @Description("Toggles vanish for yourself")
    private fun onToggleSelf(sender: Player) = toggleSelf(sender)

    @Subcommand("toggle")
    @CommandCompletion("*")
    @Description("Toggles vanish for another player")
    private fun onToggleOther(sender: Player, target: Player) {
        if (sender === target) {
            toggleSelf(sender)
        } else {
            toggleOther(target, sender)
        }
    }

    // TODO optional target?
    @Subcommand("enable")
    @CommandCompletion("*")
    @Description("Tries to enable vanish for another player")
    private fun onEnableOther(sender: Player, target: Player) {
        if (sender === target) {
            tryVanishSelf(sender, showMessage = true, showQuitMessage = false)
        } else {
            tryVanishOther(target, sender)
        }
    }

    @Subcommand("disable")
    @CommandCompletion("*")
    @Description("Tries to disable vanish for another player")
    private fun onDisableOther(sender: Player, target: Player) {
        if (sender === target) {
            tryUnvanishSelf(sender, false)
        } else {
            tryUnvanishOther(target, sender)
        }
    }

    @Subcommand("fakejoin")
    @Description("Does a 'fake join', so unvanishing and sending a join message")
    private fun onFakeJoin(sender: Player) {
        tryUnvanishSelf(sender, true)
    }

    @Subcommand("fakequit")
    @Description("Does a 'fake quit', so vanishing and sending a quit message")
    private fun onFakeQuit(sender: Player) {
        tryVanishSelf(sender, showMessage = true, showQuitMessage = true)
    }

    @Subcommand("status")
    @Description("Tells whether or not a player is vanished")
    private fun onStatus(sender: CommandSender, @Optional target: Player) {
        TODO()
    }

    @Subcommand("list")
    private fun onListVanishedUsers(sender: CommandSender) {
        if (vanished.isEmpty()) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_NOBODY_VANISHED))
        } else if (vanished.size == 1) {
            val singleUUID = vanished.first()
            sender.sendMessage("${ChatColor.GOLD}The only vanished player is: ${Bukkit.getPlayer(singleUUID)!!.name}")
        } else {
            // instead of sending multiple messages, do it in one
            val builder = StringBuilder()
            for (id in vanished) {
                if (builder.isNotEmpty()) {
                    builder.append(',')
                }
                val player = Bukkit.getPlayer(id)
                builder.append(player!!.name)
            }
            sender.sendMessage(ChatColor.GOLD.toString() + "Vanished players: ")
            sender.sendMessage(builder.toString())
        }
    }

    @HelpCommand // TODO seems probably fine
    private fun onHelp(help: CommandHelp) = help.showHelp()

    //
    // implementation
    //
    // TODO: why accepting an Entity rather than a Player?
    private fun isVanished(entity: Entity): Boolean {
        return vanished.contains(entity.uniqueId)
    }

    private fun toggleSelf(self: Player) {
        if (vanished.remove(self.uniqueId)) {
            unvanishSelfInternal(self, false)
        } else {
            tryVanishSelf(self, showMessage = true, showQuitMessage = false)
        }
    }

    private fun toggleOther(other: Player, executor: CommandSender) {
        if (vanished.remove(other.uniqueId)) {
            unvanishOtherInternal(other, executor)
        } else {
            tryVanishOther(other, executor)
        }
    }

    private fun tryVanishSelf(self: Player, showMessage: Boolean, showQuitMessage: Boolean) {
        if (!vanished.add(self.uniqueId)) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED))
            return
        }
        vanishSelfInternal(self, showMessage, showQuitMessage)
    }

    private fun vanishSelfInternal(self: Player, showMessage: Boolean, showQuitMessage: Boolean) {
        handleVanish(self, showQuitMessage)
        if (showMessage) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED))
        }
    }

    private fun tryVanishOther(other: Player, executor: CommandSender) {
        if (vanished.add(other.uniqueId)) {
            vanishOtherInternal(other, executor)
        } else {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED_OTHER))
        }
    }

    private fun vanishOtherInternal(other: Player, executor: CommandSender) {
        handleVanish(other, false)
        executor.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_FOR_OTHER, other.name))
        other.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_BY_OTHER, executor.name))
    }

    // TODO maybe implement byte that tells to not remove night vision effect afterwards
    // aka player had already that effect
    /*
    Persistent data byte explained
    0: the player is not vanished, maybe we can just remove the byte too, anyways...
    1: the player is vanished, and we didn't apply night vision
    2: the player is vanished, and we applied night vision, so remove it afterwards
    3: the player is vanished and had night vision before vanishing, so do not remove if afterwards
    */
    // TODO did i really forget to make vanished players be able to see e/o?????
    private fun handleVanish(target: Player, showQuitMessage: Boolean) {
        for (player in target.world.players) { // see onWorldChange why only the players on this world
            if (player === target || !player.canSee(target)) continue
            player.hidePlayer(plugin, target)
            if (player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) {
                // send mods a vanish announce
                player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.name))
            } else if (showQuitMessage) {
                player.sendMessage(plugin.getLang(LangKey.QUIT_MESSAGE, target.name))
            }
        }
        handleSharedStuff(target, true)
        // handle the persistent stuff to know later on if they were vanished and if they got night vision by that
        val vanishStatus = if (plugin.config.getBoolean("vanish.apply-night-vision")) 2 else 1
        if (vanishStatus == 2) { // if the player already got the effect, it will be overridden
            target.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 1, false, false, false))
        }
        val container = target.persistentDataContainer
        container.set(key, PersistentDataType.BYTE, vanishStatus.toByte())
        bossbar.addPlayer(target)
        target.saturation = 20f
        if (plugin.config.getBoolean("vanish.send-message-to-self")) {
            target.sendMessage(plugin.getLang(LangKey.QUIT_MESSAGE, target.name))
        }
    }

    private fun tryUnvanishSelf(self: Player, showJoinMessage: Boolean) {
        if (!vanished.remove(self.uniqueId)) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VISIBLE))
            return
        }
        unvanishSelfInternal(self, showJoinMessage)
    }

    private fun unvanishSelfInternal(self: Player, showJoinMessage: Boolean) {
        handleUnvanish(self, showJoinMessage)
        self.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED))
    }

    private fun tryUnvanishOther(other: Player, executor: CommandSender) {
        if (!vanished.remove(other.uniqueId)) {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_OTHER_ALREADY_VISIBLE))
            return
        }
        unvanishOtherInternal(other, executor)
    }

    private fun unvanishOtherInternal(other: Player, executor: CommandSender) {
        handleUnvanish(other, false)
        executor.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_FOR_OTHER, other.name))
        other.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_BY_OTHER, executor.name))
    }

    private fun handleUnvanish(target: Player, showJoinMessage: Boolean) {
        for (player in Bukkit.getOnlinePlayers()) { // unvanish for everyone, avoid later code worries
            if (player === target || player.canSee(target)) continue
            player.showPlayer(plugin, target)
            if (player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) {
                player.sendMessage(plugin.getLang(LangKey.VANISH_BACK_VISIBLE_ANNOUNCE, target.name))
            } else if (showJoinMessage) {
                player.sendMessage(plugin.getLang(LangKey.JOIN_MESSAGE, target.name))
            }
        }
        handleSharedStuff(target, false)

        // handle the persistent values to know if they have gotten night vision
        val container = target.persistentDataContainer
        val vanishStatus = container.getOrDefault(key, PersistentDataType.BYTE, 0.toByte())
        if (vanishStatus.toInt() == 2) {
            target.removePotionEffect(PotionEffectType.NIGHT_VISION)
        }
        // no longer vanished now so set it to 0
        container.set(key, PersistentDataType.BYTE, 0.toByte())
        target.fallDistance = -1f // TODO test
        bossbar.removePlayer(target)
        if (plugin.config.getBoolean("vanish.send-message-to-self")) {
            target.sendMessage(plugin.getLang(LangKey.JOIN_MESSAGE, target.name))
        }
    }

    private fun handleSharedStuff(target: Player, vanish: Boolean) {
        target.isInvulnerable = vanish
        target.isSleepingIgnored = vanish
        val gameMode = target.gameMode
        target.allowFlight = vanish || gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR
    }

    //
    // listeners
    // I don't want to share collections and stuff either via di or getters
    //
    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val vanishStatus = player.persistentDataContainer.getOrDefault(key, PersistentDataType.BYTE, 0.toByte())
        if (vanishStatus.toInt() == 1 || vanishStatus.toInt() == 2) { // vanished
            vanished.add(player.uniqueId)
            handleVanish(player, false)
            sendMessageToStaffYourselfExcluded(plugin.getLang(LangKey.JOIN_VANISHED, player.name), player)
            event.joinMessage = null
        } else {
            event.joinMessage = plugin.getLang(LangKey.JOIN_MESSAGE, player.name)
        }
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (vanished.remove(player.uniqueId)) {
            handleUnvanish(player, false)
            sendMessageToStaffYourselfExcluded(plugin.getLang(LangKey.QUIT_VANISHED, player.name), player)
            event.quitMessage = null
        } else {
            event.quitMessage = plugin.getLang(LangKey.QUIT_MESSAGE, player.name)
        }
    }

    private fun sendMessageToStaffYourselfExcluded(message: String, exlude: Player) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player === exlude || !player.hasPermission("xkingdoms.moderator")) continue
            player.sendMessage(message)
        }
    }

    // if players teleport to a location on another server, their fly is disabled, and they fall
    @EventHandler
    private fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (event.from.world !== player.world) {
            ensureFlying(player)
        }
    }

    // TODO check if switching to spectator stops flight too | seems to work
    // switching to survival or adventure disables flight and the player starts falling while still vanished
    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val mode = event.newGameMode
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
            ensureFlying(event.player)
        }
    }

    // if someone enters a new world, make sure that person can't see vanished people there
    @EventHandler
    private fun onWorldChange(event: PlayerChangedWorldEvent) {
        val joinedPlayer = event.player
        val vanishPlayersSeeEachOther = plugin.config.getBoolean("vanish.players-will-see-each-other")
        // if vanished players should see each other and the player who joined the world is vanished
        if (vanishPlayersSeeEachOther && isVanished(joinedPlayer)) return
        for (p in joinedPlayer.world.players) {
            if (p === joinedPlayer || !joinedPlayer.canSee(p)) continue
            // hide vanished players for the joining player
            joinedPlayer.hidePlayer(plugin, p)
        }
    }

    @EventHandler
    private fun onDeath(event: PlayerDeathEvent) { // TODO not working when using /kill command, alto it fires this event
        if (isVanished(event.entity)) {
            ensureFlying(event.entity)
        }
        Bukkit.broadcastMessage("DEBUG: PlayerDeathEvent is thrown for player's death") // todo remove
    }

    // delay a tick because it doesn't work otherwise :/
    private fun ensureFlying(target: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            target.allowFlight = true
            target.isFlying = true
        })
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val entity = event.entity
        val newFoodLevel = entity.foodLevel
        val oldFoodLevel = event.foodLevel
        if (newFoodLevel < oldFoodLevel && isVanished(entity)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onPickupItem(event: EntityPickupItemEvent) {
        if (event.entity is Player && isVanished(event.entity)) {
            event.isCancelled = true
        }
    }
}
