package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@Subcommand("sethome")
public class SethomeCommand extends BaseCommand {

    @Default
    public void onSetHome(Player player, String name) {

    }
}
