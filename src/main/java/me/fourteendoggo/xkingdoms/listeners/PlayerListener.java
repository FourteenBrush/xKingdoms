package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.management.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final UserManager userManager;

    public PlayerListener(XKingdoms plugin) {
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        if (!userManager.loadIfAbsent(event.getUniqueId())) {
            String message = "Failed to load your data, please try again in a few minutes.\nIf this problem continues, contact staff.";
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        userManager.unloadUser(player.getUniqueId());
    }

    @EventHandler
    public void onCropTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        Block clickedBlock =  event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.FARMLAND) return;

        event.getPlayer().sendMessage(ChatColor.YELLOW + "# " + ChatColor.GRAY + "Shift click to break the crops");
        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.GRAY + event.getFormat());
    }
}
