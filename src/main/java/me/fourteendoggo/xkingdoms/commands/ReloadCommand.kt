package me.fourteendoggo.xkingdoms.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.utils.Constants
import org.bukkit.command.CommandSender

@CommandAlias("reload")
@CommandPermission(Constants.ADMIN_PERMISSION_STRING)
@Description("Reloads most of the components of the plugin")
class ReloadCommand(private val plugin: XKingdoms) : BaseCommand() {
    @Default
    private fun onReload(sender: CommandSender) {
        plugin.reload()
        sender.sendMessage(plugin.getLang(LangKey.RELOAD_FINISHED))
    }
}
