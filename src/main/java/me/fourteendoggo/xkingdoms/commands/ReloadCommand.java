package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import org.bukkit.command.CommandSender;

@CommandAlias("reload")
@CommandPermission("xKingdoms.admin")
@Description("Reloads most of the components of the plugin")
public class ReloadCommand extends BaseCommand {
    private final XKingdoms plugin;

    public ReloadCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @Default
    private void onReload(CommandSender sender) {
        plugin.reloadAllComponents();
        sender.sendMessage(plugin.getLang(LangKey.RELOAD_FINISHED));
    }
}
