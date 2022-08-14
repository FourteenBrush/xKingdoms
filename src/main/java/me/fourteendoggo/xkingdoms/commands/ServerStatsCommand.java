package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("serverstats|stats")
@CommandPermission(Constants.MODERATOR_PERMISSION_STRING)
@Description("Shows information about the server")
public class ServerStatsCommand extends BaseCommand {

    @Default
    private void showServerStats(Player sender) {
        ChatColor greenColor = ChatColor.of("#00aa00");
        String outputString = """
                %s▅▅▅▅▅▅▅▅▅▅ &r SERVER STATS %s▅▅▅▅▅▅▅▅▅▅
                
                 ✸ &rCPU Name: &e%s
                 ✸ &rEntity Count: &e%s%s
                 ✸ &rTPS: &e20
                 
                %s▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅▅
                """.formatted(
                        greenColor, greenColor, System.getenv("PROCESSOR_IDENTIFIER"),
                        sender.getWorld().getEntities().size(), greenColor, greenColor
        );

        sender.sendMessage(Utils.colorizeWithHexSupport(outputString));
    }
}
