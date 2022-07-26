package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandAlias("vanish|v")
@CommandPermission("xkingdoms.moderator")
@Description("Main command for vanishing")
public class VanishCommand extends BaseCommand implements Listener {
    private final XKingdoms plugin;
    private final Set<UUID> vanished;
    private final BossBar bossbar;
    private final NamespacedKey key;

    public VanishCommand(XKingdoms plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "vanished");
        this.vanished = new HashSet<>();
        bossbar = Bukkit.createBossBar("Vanished", BarColor.BLUE, BarStyle.SOLID);
        bossbar.removeAll();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @Description("Toggles vanish for yourself")
    private void onToggleSelf(Player sender) {
        toggleSelf(sender);
    }

    @Subcommand("toggle")
    @CommandCompletion("*")
    @Description("Toggles vanish for another player")
    private void onToggleOther(Player sender, Player target) {
        if (sender == target) {
            toggleSelf(sender);
        } else {
            toggleOther(target, sender);
        }
    }

    // TODO optional target?
    @Subcommand("enable")
    @CommandCompletion("*")
    @Description("Tries to enable vanish for another player")
    private void onEnableOther(Player sender, Player target) {
        if (sender == target) {
            tryVanishSelf(sender, true, false);
        } else {
            tryVanishOther(target, sender);
        }
    }

    @Subcommand("disable")
    @CommandCompletion("*")
    @Description("Tries to disable vanish for another player")
    private void onDisableOther(Player sender, Player target) {
        if (sender == target) {
            tryUnvanishSelf(sender, false);
        } else {
            tryUnvanishOther(target, sender);
        }
    }

    @Subcommand("fakejoin")
    @Description("Does a 'fake join', so unvanishing and sending a join message")
    private void onFakeJoin(Player sender) {
        tryUnvanishSelf(sender, true);
    }

    @Subcommand("fakequit")
    @Description("Does a 'fake quit', so vanishing and sending a quit message")
    private void onFakeQuit(Player sender) {
        tryVanishSelf(sender, true, true);
    }

    @Subcommand("status")
    @Description("Tells whether or not a player is vanished")
    private void onStatus(CommandSender sender, @Optional Player target) {
        // TODO
    }

    @Subcommand("list")
    @SuppressWarnings("ConstantConditions")
    private void onListVanishedUsers(CommandSender sender) {
        if (vanished.isEmpty()) {
            sender.sendMessage(plugin.getLang(LangKey.VANISH_NOBODY_VANISHED));
        } else if (vanished.size() == 1) {
            UUID singleUUID = vanished.toArray(UUID[]::new)[0];
            sender.sendMessage(ChatColor.GOLD + "The only vanished player is : " + Bukkit.getPlayer(singleUUID).getName());
        } else {
            // instead of sending multiple messages, do it in one
            StringBuilder builder = new StringBuilder();
            for (UUID id : vanished) {
                if (!builder.isEmpty()) {
                    builder.append(',');
                }
                Player player = Bukkit.getPlayer(id);
                builder.append(player.getName());
            }
            sender.sendMessage(ChatColor.GOLD + "Vanished players: ");
            sender.sendMessage(builder.toString());
        }
    }

    @HelpCommand // TODO seems probably fine
    private void onHelp(CommandHelp help) {
        help.showHelp();
    }

    //
    // implementation
    //

    private boolean isVanished(Entity entity) {
        return vanished.contains(entity.getUniqueId());
    }

    private void toggleSelf(Player self) {
        if (vanished.remove(self.getUniqueId())) {
            unvanishSelfInternal(self, false);
        } else {
            tryVanishSelf(self, true, false);
        }
    }

    private void toggleOther(Player other, CommandSender executor) {
        if (vanished.remove(other.getUniqueId())) {
            unvanishOtherInternal(other, executor);
        } else {
            tryVanishOther(other, executor);
        }
    }

    private void tryVanishSelf(Player self, boolean showMessage, boolean showQuitMessage) {
        if (!vanished.add(self.getUniqueId())) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED));
            return;
        }
        vanishSelfInternal(self, showMessage, showQuitMessage);
    }

    private void vanishSelfInternal(Player self, boolean showMessage, boolean showQuitMessage) {
        handleVanish(self, showQuitMessage);
        if (showMessage) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED));
        }
    }

    private void tryVanishOther(Player other, CommandSender executor) {
        if (vanished.add(other.getUniqueId())) {
            vanishOtherInternal(other, executor);
        } else {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED_OTHER));
        }
    }

    private void vanishOtherInternal(Player other, CommandSender executor) {
        handleVanish(other, false);
        executor.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_FOR_OTHER, other.getName()));
        other.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_BY_OTHER, executor.getName()));
    }

    // TODO maybe implement byte that tells to not remove night vision effect afterwards
    // aka player had already that effect
    /*
    Persistent data byte explained
    0: the player is not vanished, maybe we can just remove the byte too, anyways...
    1: the player is vanished, and we didn't apply night vision
    2: the player is vanished, and we applied night vision, so remove it afterwards
    3: the player is vanished and had night vision before vanishing, so do not remove if afterwards
    */

    // TODO did i really forget to make vanished players be able to see e/o?????
    private void handleVanish(Player target, boolean showQuitMessage) {
        for (Player player : target.getWorld().getPlayers()) { // see onWorldChange why only the players on this world
            if (player == target || !player.canSee(target)) continue;
            player.hidePlayer(plugin, target);

            if (player.hasPermission("xkingdoms.moderator")) {
                // send mods a vanish announce
                player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.getName()));
            } else if (showQuitMessage) {
                player.sendMessage(plugin.getLang(LangKey.QUIT_MESSAGE, target.getName()));
            }
        }
        handleSharedStuff(target, true);
        // handle the persistent stuff to know later on if they were vanished and if they got night vision by that
        int vanishStatus = plugin.getConfig().getBoolean("vanish.apply-night-vision") ? 2 : 1;
        if (vanishStatus == 2) { // if the player already got the effect, it will be overridden
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, false));
        }
        PersistentDataContainer container = target.getPersistentDataContainer();
        container.set(key, PersistentDataType.BYTE, (byte) vanishStatus);

        bossbar.addPlayer(target);
        target.setSaturation(20);

        if (plugin.getConfig().getBoolean("vanish.send-message-to-self")) {
            target.sendMessage(plugin.getLang(LangKey.QUIT_MESSAGE, target.getName()));
        }
    }

    private void tryUnvanishSelf(Player self, boolean showJoinMessage) {
        if (!vanished.remove(self.getUniqueId())) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VISIBLE));
            return;
        }
        unvanishSelfInternal(self, showJoinMessage);
    }

    private void unvanishSelfInternal(Player self, boolean showJoinMessage) {
        handleUnvanish(self, showJoinMessage);
        self.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED));
    }

    private void tryUnvanishOther(Player other, CommandSender executor) {
        if (!vanished.remove(other.getUniqueId())) {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_OTHER_ALREADY_VISIBLE));
            return;
        }
        unvanishOtherInternal(other, executor);
    }

    private void unvanishOtherInternal(Player other, CommandSender executor) {
        handleUnvanish(other, false);
        executor.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_FOR_OTHER, other.getName()));
        other.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED_BY_OTHER, executor.getName()));
    }

    private void handleUnvanish(Player target, boolean showJoinMessage) {
        for (Player player : Bukkit.getOnlinePlayers()) { // unvanish for everyone, avoid later code worries
            if (player == target || player.canSee(target)) continue;
            player.showPlayer(plugin, target);

            if (player.hasPermission("xkingdoms.moderator")) {
                player.sendMessage(plugin.getLang(LangKey.VANISH_BACK_VISIBLE_ANNOUNCE, target.getName()));
            } else if (showJoinMessage) {
                player.sendMessage(plugin.getLang(LangKey.JOIN_MESSAGE, target.getName()));
            }
        }
        handleSharedStuff(target, false);

        // handle the persistent values to know if they have gotten night vision
        PersistentDataContainer container = target.getPersistentDataContainer();
        byte vanishStatus = container.getOrDefault(key, PersistentDataType.BYTE, (byte) 0);
        if (vanishStatus == 2) {
            target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        // no longer vanished now so set it to 0
        container.set(key, PersistentDataType.BYTE, (byte) 0);

        target.setFallDistance(-1); // TODO test
        bossbar.removePlayer(target);

        if (plugin.getConfig().getBoolean("vanish.send-message-to-self")) {
            target.sendMessage(plugin.getLang(LangKey.JOIN_MESSAGE, target.getName()));
        }
    }

    private void handleSharedStuff(Player target, boolean vanish) {
        target.setInvulnerable(vanish);
        target.setSleepingIgnored(vanish);

        GameMode gameMode = target.getGameMode();
        target.setAllowFlight(vanish || gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR);
    }

    //
    // listeners
    // I don't want to share collections and stuff either via di or getters
    //

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        byte vanishStatus = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BYTE, (byte) 0);

        if (vanishStatus == 1 || vanishStatus == 2) { // vanished
            vanished.add(player.getUniqueId());
            handleVanish(player, false);

            sendMessageToStaffYourselfExcluded(plugin.getLang(LangKey.JOIN_VANISHED, player.getName()), player);
            event.setJoinMessage(null);
        } else {
            event.setJoinMessage(plugin.getLang(LangKey.JOIN_MESSAGE, player.getName()));
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanished.remove(player.getUniqueId())) {
            handleUnvanish(player, false);
            sendMessageToStaffYourselfExcluded(plugin.getLang(LangKey.QUIT_VANISHED, player.getName()), player);
            event.setQuitMessage(null);
        } else {
            event.setQuitMessage(plugin.getLang(LangKey.QUIT_MESSAGE, player.getName()));
        }
    }

    private void sendMessageToStaffYourselfExcluded(String message, Player exlude) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == exlude || !player.hasPermission("xkingdoms.moderator")) continue;
            player.sendMessage(message);
        }
    }

    // if players teleport to a location on another server, their fly is disabled and they fall
    @EventHandler
    private void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld() != player.getWorld()) {
            ensureFlying(player);
        }
    }

    // TODO check if switching to spectator stops flight too | seems to work
    // switching to survival or adventure disables flight and the player starts falling while still vanished
    @EventHandler
    private void onGameModeChange(PlayerGameModeChangeEvent event) {
        GameMode mode = event.getNewGameMode();
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
            ensureFlying(event.getPlayer());
        }
    }

    // if someone enters a new world, make sure that person can't see vanished people there
    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        Player joinedPlayer = event.getPlayer();
        boolean vanishPlayersSeeEachOther = plugin.getConfig().getBoolean("vanish.players-will-see-each-other");
        // if vanished players should see each other and the player who joined the world is vanished
        if (vanishPlayersSeeEachOther && isVanished(joinedPlayer)) return;

        for (Player p : joinedPlayer.getWorld().getPlayers()) {
            if (p == joinedPlayer || !joinedPlayer.canSee(p)) continue;
            // hide vanished players for the joining player
            joinedPlayer.hidePlayer(plugin, p);
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) { // not working TODO
        if (isVanished(event.getEntity())) {
            ensureFlying(event.getEntity());
        }
        Bukkit.broadcastMessage("DEBUG: PlayerDeathEvent is thrown for player's death"); // todo remove
    }

    // delay a tick cuz it doesn't work otherwise :/
    private void ensureFlying(Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            target.setAllowFlight(true);
            target.setFlying(true);
        });
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        int newFoodLevel = entity.getFoodLevel();
        int oldFoodLevel = event.getFoodLevel();

        if (newFoodLevel < oldFoodLevel && isVanished(entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && isVanished(player)) {
            event.setCancelled(true);
        }
    }
}
