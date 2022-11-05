package me.fourteendoggo.xkingdoms.commands.vanish;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.utils.Constants;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.UUID;

@CommandAlias("vanish|v")
@CommandPermission(Constants.MODERATOR_PERMISSION_STRING)
public class NewVanishCommand extends BaseCommand {
    private final XKingdoms plugin;
    private final VanishManager vanishManager;

    public NewVanishCommand(XKingdoms plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    /**
     * /vanish              ok
     * /vanish on           ok
     * /vanish off          ok
     * <p>
     * /vanish on <player>  ok
     * /vanish off <player> ok
     * /vanish fakejoin
     * /vanish fakequit
     */

    @Default
    private void onToggle(Player player) {
        vanishManager.toggle(player);
    }

    @HelpCommand
    private void onHelp(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("enable|on")
    private void onVanishSelf(Player player) {
        if (vanishManager.vanish(player, true)) {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED));
        } else {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED));
        }
    }

    @Subcommand("disable|off")
    private void onUnvanishSelf(Player player) {
        if (vanishManager.unvanish(player, true, true)) {
            player.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED));
        } else {
            player.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VISIBLE));
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("enable|on")
    private void onVanishOther(CommandSender sender, Player target) {
        if (vanishManager.vanish(target, true)) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_FOR_OTHER, target.getName()));
        } else {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED_OTHER, target.getName()));
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("disable|off")
    private void onUnvanishOther(CommandSender sender, Player target) {
        if (vanishManager.unvanish(target, true, true)) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_FOR_OTHER, target.getName()));
        } else {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_OTHER_ALREADY_VISIBLE, target.getName()));
        }
    }

    @Subcommand("list")
    private void onList(CommandSender sender) {
        Iterator<UUID> iterator = vanishManager.getVanishedPlayers();
        if (!iterator.hasNext()) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_NOBODY_VANISHED));
            return;
        }

        StringBuilder builder = new StringBuilder();
        iterator.forEachRemaining(uuid -> {
            Player vanishedPlayer = Bukkit.getPlayer(uuid);
            Validate.isTrue(vanishedPlayer != null && vanishedPlayer.isOnline()); // should be true but anyway

            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(vanishedPlayer.getName());
        });

        sender.sendMessage(ChatColor.GOLD + "Vanished players: " + builder);
    }
}
