package me.fourteendoggo.xkingdoms.commands.vanish;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Reloadable;
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
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Reloadable {
    /**
     * Player is not vanished but has vanish flag set
     * This means that the player has been vanishing before and is now back visible
     */
    private static final byte NOT_VANISHED = 0;
    /**
     * Player is vanished and the config stated that we should give them night vision
     */
    private static final byte VANISHED_WITH_NIGHT_VISION = 1;
    /**
     * Player is vanished and we didn't give them night vision
     */
    private static final byte VANISHED_WITHOUT_NIGHT_VISION = 2;
    private final XKingdoms plugin;
    private final NamespacedKey key;
    private final BossBar bossBar;
    private final Set<UUID> vanished;

    public VanishManager(XKingdoms plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "vanished");
        this.bossBar = Bukkit.createBossBar("Vanished", getBossBarColor(), BarStyle.SOLID);
        this.vanished = new HashSet<>();

        bossBar.removeAll(); // just to be sure
    }

    @Override
    public void reload() {
        BarColor color = getBossBarColor();
        bossBar.setColor(color);
    }

    private BarColor getBossBarColor() {
        String color = plugin.getConfig().getString("vanish.bossbar-color");
        if (color == null || color.isBlank()) {
            return BarColor.PURPLE;
        }
        return switch (color.toLowerCase()) {
            case "pink" -> BarColor.PINK;
            case "blue" -> BarColor.BLUE;
            case "red" -> BarColor.RED;
            case "green" -> BarColor.GREEN;
            case "yellow" -> BarColor.YELLOW;
            case "white" -> BarColor.WHITE;
            default -> BarColor.PURPLE;
        };
    }

    public void destroyState() {
        bossBar.removeAll(); // doing this ensures that boss bars do not appear twice after reloading
    }

    private boolean isValidVanishFlag(byte flag) {
        return flag == VANISHED_WITH_NIGHT_VISION || flag == VANISHED_WITHOUT_NIGHT_VISION;
    }

    public Iterator<UUID> getVanishedPlayers() {
        return vanished.iterator();
    }

    public boolean vanishIfFlagged(Player target) {
        PersistentDataContainer pdc = target.getPersistentDataContainer();
        byte flag = pdc.getOrDefault(key, PersistentDataType.BYTE, (byte) -1);

        if (!isValidVanishFlag(flag)) {
            return false;
        }
        return vanish(target, true, false, flag);
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public void toggle(Player target) {
        if (unvanish(target, true, true)) {
            target.sendMessage(plugin.getLang(LangKey.VANISH_DISABLED));
        } else {
            vanish(target, false); // false cuz unvanish() already checked and returned false
            target.sendMessage(plugin.getLang(LangKey.VANISH_ENABLED));
        }
    }

    public boolean vanish(Player target, boolean check) {
        byte vanishFlag = plugin.getConfig().getBoolean("vanish.apply-night-vision")
                ? VANISHED_WITH_NIGHT_VISION
                : VANISHED_WITHOUT_NIGHT_VISION;
        return vanish(target, check, true, vanishFlag);
    }

    public boolean vanish(Player target, boolean check, boolean notifyStaff, byte vanishFlag) {
        if (!vanished.add(target.getUniqueId()) && check) { // ensure set has a valid state
            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == target || !player.canSee(target)) continue;
            player.hidePlayer(plugin, target);

            if (notifyStaff && !player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue;
            player.sendMessage(plugin.getLang(LangKey.VANISH_ANNOUNCE, target.getName()));
        }
        changePlayerState(target, true);

        if (vanishFlag == VANISHED_WITH_NIGHT_VISION) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, false));
        }
        target.getPersistentDataContainer().set(key, PersistentDataType.BYTE, vanishFlag);

        bossBar.addPlayer(target);
        target.setSaturation(20);
        target.setExhaustion(0);
        return true;
    }

    public boolean unvanish(Player target, boolean check, boolean makePersistent) {
        if (!vanished.remove(target.getUniqueId()) && check) { // ensure set has a valid state
            return false;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == target || !player.canSee(target)) continue;
            if (!player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) continue;

            player.sendMessage(plugin.getLang(LangKey.VANISH_BACK_VISIBLE_ANNOUNCE, target.getName()));
        }
        changePlayerState(target, false);
        bossBar.removePlayer(target);

        PersistentDataContainer pdc = target.getPersistentDataContainer();
        byte vanishFlag = pdc.getOrDefault(key, PersistentDataType.BYTE, (byte) -1);

        if (vanishFlag == VANISHED_WITHOUT_NIGHT_VISION) {
            target.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        if (makePersistent) {
            pdc.set(key, PersistentDataType.BYTE, NOT_VANISHED);
        }
        return true;
    }

    private void changePlayerState(Player target, boolean vanish) {
        target.setInvulnerable(vanish);
        target.setSleepingIgnored(vanish);

        target.setAllowFlight(vanish || switch (target.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        });
    }
}
