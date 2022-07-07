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
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID id = event.getUniqueId();

        if (!userRepository.has(id)) { // block async thread
            plugin.getDatabase().loadUser(id).thenAccept(userRepository::put).join();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        userRepository.get(player.getUniqueId()).login(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        KingdomPlayer user = userRepository.remove(event.getPlayer().getUniqueId());
        user.logout();
        plugin.getDatabase().saveUser(user);
    }
}
