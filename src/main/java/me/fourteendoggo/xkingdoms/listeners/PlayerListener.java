package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.repository.UserRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final XKingdoms plugin;
    private final UserRepository userRepository;

    public PlayerListener(XKingdoms plugin) {
        this.plugin = plugin;
        this.userRepository = plugin.getUserRepository();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            plugin.getDatabase().loadUser(event.getUniqueId()).thenAccept(player -> {
                // userRepository.add(player);
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        KingdomPlayer player = userRepository.remove(uuid);

        player.logout();
        plugin.getDatabase().saveUser(player);
    }
}
