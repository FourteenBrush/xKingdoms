package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.repository.UserRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class PlayerListener implements Listener {
    private final XKingdoms plugin;
    private final UserRepository userRepository;

    public PlayerListener(XKingdoms plugin) {
        this.plugin = plugin;
        this.userRepository = plugin.getUserRepository();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        if (userRepository.contains(event.getUniqueId())) return;

        KingdomPlayer player;
        try {
            player = plugin.getDatabase().loadPlayerSync(event.getUniqueId());
            userRepository.put(player);
        } catch (Exception e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Failed to load your data, please try again in a few minutes");
            plugin.getLogger().log(Level.SEVERE, "Failed to load the player with uuid " + event.getUniqueId());
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
        KingdomPlayer kPlayer = userRepository.get(player.getUniqueId());
        kPlayer.login(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        KingdomPlayer kPlayer = userRepository.remove(player.getUniqueId());
        kPlayer.logout();
        plugin.getDatabase().savePlayer(kPlayer);
    }
}
