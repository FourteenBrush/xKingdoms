package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.commands.vanish.VanishManager;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.storage.management.UserManager;
import me.fourteendoggo.xkingdoms.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {
    private final XKingdoms plugin;
    private final VanishManager vanishManager;
    private final UserManager userManager;

    public PlayerListener(XKingdoms plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        if (!userManager.loadIfAbsent(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Failed to load your data, please try again in a few minutes.\nIf this problem continues, contact staff.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        // check if the player was vanished before quitting
        if (vanishManager.vanishIfFlagged(joinedPlayer)) {
            // notify staff a vanished player has been joined
            notifyStaff(joinedPlayer, plugin.getLang(LangKey.JOIN_VANISHED, joinedPlayer.getName()));
            event.setJoinMessage(null);
        } else {
            // normal player join
            event.setJoinMessage(plugin.getLang(LangKey.JOIN_MESSAGE, joinedPlayer.getName()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player quitPlayer = event.getPlayer();
        userManager.unloadUser(quitPlayer.getUniqueId());
        // unvanish if player was vanished, re-vanish when rejoining
        if (vanishManager.unvanish(quitPlayer, true, false)) {
            // inform staff a vanished player has been quit
            notifyStaff(quitPlayer, plugin.getLang(LangKey.QUIT_VANISHED, quitPlayer.getName()));
            event.setQuitMessage(null);
        } else {
            event.setQuitMessage(plugin.getLang(LangKey.QUIT_MESSAGE, quitPlayer.getName()));
        }
    }

    private void notifyStaff(Player ignoredPlayer, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == ignoredPlayer || !player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue;

            player.sendMessage(message);
        }
    }

    private void ensureFlying(Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            target.setAllowFlight(true);
            target.setFlying(true);
        });
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld() != event.getTo().getWorld() && vanishManager.isVanished(player)) {
            ensureFlying(player);
        }
    }

    @EventHandler
    private void onGameModeChange(PlayerGameModeChangeEvent event) {
        GameMode newGameMode = event.getNewGameMode();
        if ((newGameMode == GameMode.SURVIVAL || newGameMode == GameMode.ADVENTURE) && vanishManager.isVanished(event.getPlayer())) {
            ensureFlying(event.getPlayer());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player joinedPlayer = event.getPlayer();
        if (plugin.getConfig().getBoolean("vanish.see-other-vanished-players") && vanishManager.isVanished(joinedPlayer)) return;

        for (Player player : event.getFrom().getPlayers()) {
            if (player == joinedPlayer || !joinedPlayer.canSee(player)) continue;
            // hide vanished players from the joined player, only needed in this world
            joinedPlayer.hidePlayer(plugin, player);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        int newFoodLevel = event.getFoodLevel();
        int oldFoodLevel = player.getFoodLevel();

        if (newFoodLevel < oldFoodLevel && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!vanishManager.isVanished(player)) return;

        String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.endsWith("fell out of the world")) {
            // allowing fly immediately after /kill got executed does not seem to work, so we delay it
            Bukkit.getScheduler().runTaskLater(plugin, () -> ensureFlying(player), 1);
        } else {
            ensureFlying(player);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // TODO add Settings::allow-pickup check
        if (event.getEntity() instanceof Player player && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.GRAY + event.getFormat()); // go away ugly white color
    }
}
