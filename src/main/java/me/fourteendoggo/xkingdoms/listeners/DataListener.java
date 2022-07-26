package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DataListener implements Listener {
    private final XKingdoms plugin;

    public DataListener(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        if (plugin.getUserRepository().contains(event.getUniqueId())) return;

        KingdomPlayer player = plugin.getDatabase().loadPlayerSync(event.getUniqueId());
        if (player != null) {
            plugin.getUserRepository().put(player);
        } else {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Failed to load your data, please try again in a few minutes");
        }

        /*
        // block async thread
        plugin.getDatabase().loadPlayer(event.getUniqueId())
                .thenAccept(userRepository::put)
                .exceptionally(t -> {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Failed to load your data, please try again in a few minutes");
                    return null;
                }).join();
         */
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        KingdomPlayer kPlayer = plugin.getUserRepository().get(player.getUniqueId());
        kPlayer.login(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        KingdomPlayer kPlayer = plugin.getUserRepository().remove(player.getUniqueId());
        kPlayer.logout();
        plugin.getDatabase().savePlayer(kPlayer);
    }
}
