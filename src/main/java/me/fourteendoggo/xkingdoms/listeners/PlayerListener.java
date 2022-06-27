package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerListener implements Listener {
    private final XKingdoms plugin;

    public PlayerListener(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // load em
            Player p = null;
            plugin.getLogger().info("a player passed the async pre login, not me");
        }
    }
}
