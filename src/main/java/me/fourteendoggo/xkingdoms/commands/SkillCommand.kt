package me.fourteendoggo.xkingdoms.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import me.fourteendoggo.xkingdoms.skill.SkillProgress
import me.fourteendoggo.xkingdoms.skill.SkillType
import me.fourteendoggo.xkingdoms.utils.Utils
import org.bukkit.entity.Player

@CommandAlias("skill|skills")
class SkillCommand : BaseCommand() {
    @Default
    @Description("Shows an overview of your skills progress")
    private fun onListSkills(player: Player, kPlayer: KingdomPlayer) {
        val playerData = kPlayer.data
        val builder = StringBuilder()
        builder.append("&e------------ &7[&eSkills&7] &e------------\n&7Below is a list of all your skills:")
        playerData.skills.forEach { (type: SkillType, progress: SkillProgress) ->
            builder.append("\n&6  ${type.displayName}: level: ${progress.level}, xp: ${progress.xp}")
        }
        player.sendMessage(Utils.colorize(builder.toString()))
    }
}
