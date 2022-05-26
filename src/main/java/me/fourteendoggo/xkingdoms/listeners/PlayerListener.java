package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.Xkingdoms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerListener implements Listener {
    private final Xkingdoms plugin;

    public PlayerListener(Xkingdoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // load em
            plugin.getLogger().info("a player passed the async pre login, not me");
        }
    }
}
