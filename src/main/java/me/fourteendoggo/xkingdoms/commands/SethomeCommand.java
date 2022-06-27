package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.entity.Player;

@CommandAlias("sethome")
public class SethomeCommand extends BaseCommand {
    private final XKingdoms plugin;

    public SethomeCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onSetHome(Player player, String name) {

    }
}
