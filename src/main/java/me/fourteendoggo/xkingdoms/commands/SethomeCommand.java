package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.bukkit.entity.Player;

@CommandAlias("sethome")
public class SethomeCommand extends BaseCommand {

    @Default
    public void onSetHome(Player sender, String name) {
        KingdomPlayer player = null; // someMap.get(player.getUniqueId())

        sender.sendMessage("created a home");
    }
}
