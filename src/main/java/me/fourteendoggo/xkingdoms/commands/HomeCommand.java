package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HomeCommand extends BaseCommand {
    private final XKingdoms plugin;

    public HomeCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("sethome")
    public void onSetHome(Player player, String name) {
        Bukkit.broadcastMessage("set home " + name);
    }

    @CommandAlias("delhome")
    public void onDeleteHome(Player player, String name) {
        Bukkit.broadcastMessage("deleted home " + name);
    }
}
