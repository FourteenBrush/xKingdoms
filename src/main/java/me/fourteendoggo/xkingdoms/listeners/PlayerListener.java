package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.persistence.Storage;
import me.fourteendoggo.xkingdoms.storage.repository.impl.UserCache;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Storage storage;
    private final UserCache userRepository;

    public PlayerListener(XKingdoms plugin) {
        this.storage = plugin.getStorage();
        this.userRepository = plugin.getUserCache();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        if (userRepository.contains(event.getUniqueId())) return;

        KingdomPlayer player = storage.loadPlayerSync(event.getUniqueId());
        if (player != null) {
            userRepository.put(player);
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
        userRepository.get(player.getUniqueId()).login();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        KingdomPlayer kPlayer = userRepository.remove(player.getUniqueId());
        kPlayer.logout();
        storage.savePlayer(kPlayer); // done async
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.GRAY + event.getFormat());
    }
}
