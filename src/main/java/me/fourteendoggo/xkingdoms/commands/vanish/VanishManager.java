package me.fourteendoggo.xkingdoms.commands.vanish;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private static final byte NIGHT_VISION_APPLIED = 0x1;
    private static final byte N0_NIGHT_VISION = 0x2;
    private final XKingdoms plugin;
    private final NamespacedKey key;
    private final BossBar bossBar;
    private final Set<UUID> vanished;

    public VanishManager(XKingdoms plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "vanished");
        this.bossBar = Bukkit.createBossBar("Vanished", BarColor.RED, BarStyle.SOLID);
        this.vanished = new HashSet<>();

        bossBar.removeAll();
    }

    public void toggle(Player player) {


        if (vanished.remove(player.getUniqueId())) {
            unvanish(player, false);
            player.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED));
        } else {
            vanish(player, true);
            player.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED));
        }
    }

    private byte getVanishStatus() {
        return plugin.getConfig().getBoolean("vanish.apply-night-vision") ? NIGHT_VISION_APPLIED : N0_NIGHT_VISION;
    }

    private boolean vanish(Player target, boolean check) {
        if (check && !vanished.add(target.getUniqueId())) {
            return false;
        }

        for (Player player : target.getWorld().getPlayers()) {
            if (player == target || !player.canSee(target)) continue;
            player.hidePlayer(plugin, target);

            if (player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) {
                player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.getName()));
            }
        }
        changePlayerState(target, true);

        byte vanishStatus = getVanishStatus();
        if (vanishStatus == N0_NIGHT_VISION) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, false));
        }
        target.getPersistentDataContainer().set(key, PersistentDataType.BYTE, vanishStatus);

        bossBar.addPlayer(target);
        target.setSaturation(20);
        return true;
    }

    private boolean unvanish(Player target, boolean check) {
        if (check && !vanished.remove(target.getUniqueId())) {
            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == target || !player.canSee(target)) continue;

            if (player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) {
                player.sendMessage(plugin.getLang(LangKey.VANISH_BACK_VISIBLE_ANNOUNCE, target.getName()));
            }
        }
        changePlayerState(target, false);

        PersistentDataContainer pdc = target.getPersistentDataContainer();
        byte vanishStatus = pdc.getOrDefault(key, PersistentDataType.BYTE, (byte) 0);
        if (vanishStatus == N0_NIGHT_VISION) {
            target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        return true;
    }

    private void changePlayerState(Player player, boolean vanish) {
        player.setInvulnerable(vanish);
        player.setSleepingIgnored(vanish);

        player.setAllowFlight(vanish || switch (player.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        });
    }
}
