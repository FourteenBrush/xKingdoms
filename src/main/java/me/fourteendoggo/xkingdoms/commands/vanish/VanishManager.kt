package me.fourteendoggo.xkingdoms.commands.vanish

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.utils.Constants
import me.fourteendoggo.xkingdoms.utils.Reloadable
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class VanishManager(private val plugin: XKingdoms) : Reloadable {
    private val key = NamespacedKey(plugin, "vanished")
    private val bossBar: BossBar
    private val vanished = HashSet<UUID>()

    init {
        bossBar = Bukkit.createBossBar("Vanished", bossBarColor, BarStyle.SOLID)
        bossBar.removeAll() // just to be sure
    }

    override fun reload() {
        bossBar.color = bossBarColor
    }

    private val bossBarColor: BarColor
        get() {
            val color = plugin.config.getString("vanish.bossbar-color")
            return if (color.isNullOrBlank()) {
                BarColor.PURPLE
            } else when (color.lowercase()) {
                "pink" -> BarColor.PINK
                "blue" -> BarColor.BLUE
                "red" -> BarColor.RED
                "green" -> BarColor.GREEN
                "yellow" -> BarColor.YELLOW
                "white" -> BarColor.WHITE
                else -> BarColor.PURPLE
            }
        }

    fun destroyState() = bossBar.removeAll() // doing this ensures that boss bars do not appear twice after reloading

    private fun isValidVanishFlag(flag: Byte): Boolean {
        return flag == VANISHED_WITH_NIGHT_VISION || flag == VANISHED_WITHOUT_NIGHT_VISION
    }

    val vanishedPlayers: Iterator<UUID>
        get() = vanished.iterator()

    fun vanishIfFlagged(target: Player): Boolean {
        val pdc = target.persistentDataContainer
        val flag = pdc.getOrDefault(key, PersistentDataType.BYTE, -1).toByte()
        return if (!isValidVanishFlag(flag)) {
            false
        } else vanish(target, check = true, notifyStaff = false, flag)
    }

    fun isVanished(player: Player): Boolean {
        return vanished.contains(player.uniqueId)
    }

    fun toggle(target: Player) {
        if (unvanish(target, check = true, makePersistent = true)) {
            target.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED))
        } else {
            vanish(target, false) // false because unvanish() already checked and returned false
            target.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED))
        }
    }

    fun vanish(target: Player, check: Boolean): Boolean {
        val vanishFlag = when (plugin.config.getBoolean("vanish.apply-night-vision")) {
            true -> VANISHED_WITH_NIGHT_VISION
            false -> VANISHED_WITHOUT_NIGHT_VISION
        }
        return vanish(target, check, true, vanishFlag)
    }

    private fun vanish(target: Player, check: Boolean, notifyStaff: Boolean, vanishFlag: Byte): Boolean {
        if (!vanished.add(target.uniqueId) && check) { // ensure set has a valid state
            return false
        }
        for (player in Bukkit.getOnlinePlayers()) {
            if (player === target || !player.canSee(target)) continue
            player.hidePlayer(plugin, target)
            if (notifyStaff && !player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue
            player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.name))
        }
        changePlayerState(target, true)
        if (vanishFlag == VANISHED_WITH_NIGHT_VISION) {
            target.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 1, false, false, false))
        }
        target.persistentDataContainer.set(key, PersistentDataType.BYTE, vanishFlag)
        bossBar.addPlayer(target)
        target.saturation = 20f
        target.exhaustion = 0f
        return true
    }

    fun unvanish(target: Player, check: Boolean, makePersistent: Boolean): Boolean {
        if (!vanished.remove(target.uniqueId) && check) { // ensure set has a valid state
            return false
        }
        for (player in Bukkit.getOnlinePlayers()) {
            if (player === target || !player.canSee(target)) continue
            if (!player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue
            player.sendMessage(plugin.getLang(LangKey.VANISH_BACK_VISIBLE_ANNOUNCE, target.name))
        }
        changePlayerState(target, false)
        bossBar.removePlayer(target)
        val pdc = target.persistentDataContainer
        val vanishFlag = pdc.getOrDefault(key, PersistentDataType.BYTE, -1).toByte()
        if (vanishFlag == VANISHED_WITHOUT_NIGHT_VISION) {
            target.removePotionEffect(PotionEffectType.NIGHT_VISION)
        }
        if (makePersistent) {
            pdc.set(key, PersistentDataType.BYTE, NOT_VANISHED)
        }
        return true
    }

    private fun changePlayerState(target: Player, vanish: Boolean) {
        target.isInvulnerable = vanish
        target.isSleepingIgnored = vanish
        target.allowFlight = vanish || when (target.gameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> true
            else -> false
        }
    }

    companion object {
        /**
         * Player is not vanished but has vanish flag set
         * This means that the player has been vanishing before and is now back visible
         */
        private const val NOT_VANISHED: Byte = 0

        /**
         * Player is vanished and the config stated that we should give them night vision
         */
        private const val VANISHED_WITH_NIGHT_VISION: Byte = 1

        /**
         * Player is vanished and we didn't give them night vision
         */
        private const val VANISHED_WITHOUT_NIGHT_VISION: Byte = 2
    }
}
