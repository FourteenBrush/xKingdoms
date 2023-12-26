package me.fourteendoggo.xkingdoms.commands.vanish

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.utils.Constants
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("vanish|v")
@CommandPermission(Constants.MODERATOR_PERMISSION_STRING)
class NewVanishCommand(
        private val plugin: XKingdoms,
        private val vanishManager: VanishManager,
) : BaseCommand() {

    /**
     * /vanish              ok
     * /vanish on           ok
     * /vanish off          ok
     *
     *
     * /vanish on <player>  ok
     * /vanish off <player> ok
     * /vanish fakejoin
     * /vanish fakequit
    </player></player> */
    @Default
    private fun onToggle(player: Player) = vanishManager.toggle(player)

    @HelpCommand
    private fun onHelp(help: CommandHelp) = help.showHelp()

    @Subcommand("enable|on")
    private fun onVanishSelf(player: Player) {
        if (vanishManager.vanish(player, true)) {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED))
        } else {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED))
        }
    }

    @Subcommand("disable|off")
    private fun onUnvanishSelf(player: Player) {
        if (vanishManager.unvanish(player, check = true, makePersistent = true)) {
            player.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED))
        } else {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VISIBLE))
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("enable|on")
    private fun onVanishOther(sender: CommandSender, target: Player) {
        if (vanishManager.vanish(target, true)) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_FOR_OTHER, target.name))
        } else {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED_OTHER, target.name))
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("disable|off")
    private fun onUnvanishOther(sender: CommandSender, target: Player) {
        if (vanishManager.unvanish(target, check = true, makePersistent = true)) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_FOR_OTHER, target.name))
        } else {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_OTHER_ALREADY_VISIBLE, target.name))
        }
    }

    @Subcommand("list")
    private fun onList(sender: CommandSender) {
        val iterator = vanishManager.vanishedPlayers
        if (!iterator.hasNext()) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_NOBODY_VANISHED))
            return
        }
        val builder = StringBuilder()
        iterator.forEachRemaining { uuid ->
            val vanishedPlayer = Bukkit.getPlayer(uuid)
            Validate.isTrue(vanishedPlayer != null && vanishedPlayer.isOnline) // should be true but anyway
            if (builder.isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(vanishedPlayer!!.name)
        }
        sender.sendMessage(ChatColor.GOLD.toString() + "Vanished players: " + builder)
    }
}
