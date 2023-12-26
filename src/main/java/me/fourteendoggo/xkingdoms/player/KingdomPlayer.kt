package me.fourteendoggo.xkingdoms.player

import me.fourteendoggo.xkingdoms.skill.SkillType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import java.util.UUID

class KingdomPlayer(val uuid: UUID, val playerData: PlayerData) {
    private val skillsProgressBar = Bukkit.createBossBar("Progress: ", BarColor.PINK, BarStyle.SOLID)
    private val player by lazy { Bukkit.getPlayer(uuid)!! }

    constructor(uuid: UUID) : this(uuid, PlayerData(0))

    fun invalidate() = skillsProgressBar.removeAll()

    fun showProgress(type: SkillType, currentXp: Int, maxXp: Int) = skillsProgressBar.run {
        setTitle("${type.displayName}: $currentXp/$maxXp")
        addPlayer(player)
    }

    fun levelUpSkill(type: SkillType, reachedLevel: Int) = player.run {
        playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        sendMessage("${ChatColor.DARK_PURPLE}Congratulations! You reached level $reachedLevel in ${type.displayName}")
    }
}