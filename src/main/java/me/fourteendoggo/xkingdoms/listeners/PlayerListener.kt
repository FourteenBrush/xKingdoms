package me.fourteendoggo.xkingdoms.listeners

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.commands.vanish.VanishManager
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.utils.Constants
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

@Suppress("UNUSED")
class PlayerListener(private val plugin: XKingdoms, private val vanishManager: VanishManager) : Listener {
    private val userManager = plugin.userManager

    @EventHandler
    fun onAsyncPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return
        if (!userManager.loadIfAbsent(event.uniqueId)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "Failed to load your data, please try again in a few minutes.\nIf this problem continues, contact staff.",
            )
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val joinedPlayer = event.player
        // check if the player was vanished before quitting
        if (vanishManager.vanishIfFlagged(joinedPlayer)) {
            // notify staff a vanished player has been joined
            notifyStaff(joinedPlayer, plugin.getLang(LangKey.JOIN_VANISHED, joinedPlayer.name))
            event.joinMessage = null
        } else {
            // normal player join
            event.joinMessage = plugin.getLang(LangKey.JOIN_MESSAGE, joinedPlayer.name)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val quitPlayer = event.player
        userManager.unloadUser(quitPlayer.uniqueId)
        // unvanish if player was vanished, re-vanish when rejoining
        if (vanishManager.unvanish(quitPlayer, check= true, makePersistent = false)) {
            // inform staff a vanished player has been quit
            notifyStaff(quitPlayer, plugin.getLang(LangKey.QUIT_VANISHED, quitPlayer.name))
            event.quitMessage = null
        } else {
            event.quitMessage = plugin.getLang(LangKey.QUIT_MESSAGE, quitPlayer.name)
        }
    }

    private fun notifyStaff(ignoredPlayer: Player, message: String) {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player === ignoredPlayer || !player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue
            player.sendMessage(message)
        }
    }

    private fun ensureFlying(target: Player) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            target.allowFlight = true
            target.isFlying = true
        })
    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (event.from.world !== event.to!!.world && vanishManager.isVanished(player)) {
            ensureFlying(player)
        }
    }

    @EventHandler
    private fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val newGameMode = event.newGameMode
        if ((newGameMode == GameMode.SURVIVAL || newGameMode == GameMode.ADVENTURE) && vanishManager.isVanished(event.player)) {
            ensureFlying(event.player)
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val joinedPlayer = event.player
        if (plugin.config.getBoolean("vanish.see-other-vanished-players") && vanishManager.isVanished(joinedPlayer)) return
        for (player in event.from.players) {
            if (player === joinedPlayer || !joinedPlayer.canSee(player)) continue
            // hide vanished players from the joined player, only needed in this world
            joinedPlayer.hidePlayer(plugin, player)
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val player = event.entity as Player
        val newFoodLevel = event.foodLevel
        val oldFoodLevel = player.foodLevel
        if (newFoodLevel < oldFoodLevel && vanishManager.isVanished(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (!vanishManager.isVanished(player)) return
        val deathMessage = event.deathMessage
        if (deathMessage != null && deathMessage.endsWith("fell out of the world")) {
            // allowing fly immediately after /kill got executed does not seem to work, so we delay it
            Bukkit.getScheduler().runTaskLater(plugin, Runnable { ensureFlying(player) }, 1)
        } else {
            ensureFlying(player)
        }
    }

    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        // TODO add Settings::allow-pickup check
        if (event.entity is Player && vanishManager.isVanished(event.entity as Player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        event.format = "${ChatColor.GRAY}${event.format}" // get rid of ugly white color
    }
}
