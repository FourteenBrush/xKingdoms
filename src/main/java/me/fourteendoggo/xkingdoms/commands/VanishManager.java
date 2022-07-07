package me.fourteendoggo.xkingdoms.commands;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final XKingdoms plugin;
    private final NamespacedKey key;
    private final Set<UUID> vanished;
    private final BossBar bossbar;

    public VanishManager(XKingdoms plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "vanished");
        this.vanished = new HashSet<>();
        bossbar = Bukkit.createBossBar("Vanished", BarColor.BLUE, BarStyle.SOLID);
    }

    private boolean isVanished(Entity entity) {
        return vanished.contains(entity.getUniqueId());
    }

    private boolean vanishSelf(Player self, boolean showMessage, boolean showQuitMessage) {
        if (!vanished.add(self.getUniqueId())) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED));
            return false;
        }
        vanishSelfInternal(self, showMessage, showQuitMessage);
        return true;
    }

    private void vanishSelfInternal(Player self, boolean showMessage, boolean showQuitMessage) {
        handleVanish(self, showQuitMessage);
        if (showMessage) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED));
        }
    }

    private boolean vanishOther(Player other, CommandSender executor) {
        if (!vanished.add(other.getUniqueId())) {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VANISHED_OTHER));
            return false;
        }
        vanishOtherInternal(other, executor);
        return true;
    }

    private void vanishOtherInternal(Player other, CommandSender executor) {
        handleVanish(other, false);
        executor.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_FOR_OTHER, other.getName()));
        other.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED_BY_OTHER, executor.getName()));
    }

    private void handleVanish(Player target, boolean showQuitMessage) {
        for (Player player : target.getWorld().getPlayers()) { // see onWorldChange why only the players on this world
            if (player == target || !player.canSee(target)) continue;
            player.hidePlayer(plugin, target);

            if (player.hasPermission("xkingdoms.moderator")) {
                player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.getName()));
            } else if (showQuitMessage) {
                player.sendMessage(plugin.getLang(LangKey.QUIT_MESSAGE, target.getName()));
            }
        }
        handleSharedStuff(target, true);
        // handle the persistent stuff to know later on if they were vanished and if they got night vision by that
        int vanishStatus = plugin.getConfig().getBoolean("vanish.apply-nightvision") ? 2 : 1;
        if (vanishStatus == 2) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, false));
        }
        PersistentDataContainer container = target.getPersistentDataContainer();
        container.set(key, PersistentDataType.BYTE, (byte) vanishStatus);

        bossbar.addPlayer(target);
        target.setSaturation(20f);
    }

    private boolean unvanishSelf(Player self, boolean showJoinMessage) {
        if (!vanished.remove(self.getUniqueId())) {
            self.sendMessage(plugin.getLang(LangKey.VANISH_ALREADY_VISIBLE));
            return false;
        }
        unvanishSelfInternal(self, showJoinMessage);
        return true;
    }

    private void unvanishSelfInternal(Player self, boolean showJoinMessage) {
        handleUnvanish(self, showJoinMessage);
        self.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED));
    }

    private boolean unvanishOther(Player other, CommandSender executor) {
        if (!vanished.remove(other.getUniqueId())) {
            executor.sendMessage(plugin.getLang(LangKey.VANISH_OTHER_ALREADY_VISIBLE));
            return false;
        }
        unvanishOtherInternal(other, executor);
        return true;
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

        bossbar.removePlayer(target);
    }

    private void handleSharedStuff(Player target, boolean vanish) {
        target.setInvulnerable(vanish);
        target.setSleepingIgnored(vanish);

        target.setAllowFlight(vanish || switch (target.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        });
    }
}
