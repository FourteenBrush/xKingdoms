package me.fourteendoggo.xkingdoms;

import co.aikar.commands.BukkitCommandManager;
import me.fourteendoggo.xkingdoms.commands.SethomeCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Xkingdoms extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new SethomeCommand());

        getLogger().info("xKingdoms has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("xKingdoms has been disabled.");
    }
}
