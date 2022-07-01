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
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        UUID id = event.getUniqueId();

        KingdomPlayer user = userRepository.get(id);
        if (user != null) {
            user.login();
            return;
        }
        plugin.getDatabase().loadUser(id).thenAccept(player -> {
            userRepository.put(player);
            player.login();
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        KingdomPlayer user = userRepository.remove(event.getPlayer().getUniqueId());

        user.logout();
        plugin.getDatabase().saveUser(user);
    }

    /*
    when an user joins -> load it from db and put it into cache
    when they leave -> save them to db (cuz when server crashes data is gone) but do not remove them from cache
    directly (delayed removal)
    so delayed removal prevents unnessecary db load operations
    just do stuff like you would normally do but the backend is optimized
     */
}
