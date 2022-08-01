package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {
    private final XKingdoms plugin;

    public ReloadCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("reload")
    private void onReload(CommandSender sender) {
        plugin.reloadAllComponents();
        sender.sendMessage(plugin.getLang(LangKey.RELOAD_FINISHED));
    }
}
