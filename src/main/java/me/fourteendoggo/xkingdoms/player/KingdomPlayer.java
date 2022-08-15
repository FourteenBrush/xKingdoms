package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.skill.SkillType;
import me.fourteendoggo.xkingdoms.utils.LazyValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KingdomPlayer {
    private final UUID uuid;
    private final PlayerData playerData;
    private final transient BossBar skillProgressBar;
    private final transient LazyValue<Player> playerGetter;

    public KingdomPlayer(UUID uuid, PlayerData playerData) {
        this.uuid = uuid;
        this.playerData = playerData;
        skillProgressBar = Bukkit.createBossBar("Progress: ", BarColor.PINK, BarStyle.SOLID);
        skillProgressBar.removeAll();
        this.playerGetter = new LazyValue<>(() -> Bukkit.getPlayer(uuid));
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerData getData() {
        return playerData;
    }

    public Player getPlayer() {
        return playerGetter.get();
    }

    public void invalidate() {
        playerGetter.clear();
    }

    public void showProgress(SkillType type, int currentXP, int maxXP) {
        skillProgressBar.setTitle("%s: %s/%s".formatted(type.getDisplayName(), currentXP, maxXP));
        skillProgressBar.addPlayer(getPlayer());
    }

    public void levelUpSkill(SkillType type, int reachedLevel) {
        Player player = getPlayer();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

        player.sendMessage(ChatColor.DARK_PURPLE + "Congratulations! You reached level %s in %s".formatted(reachedLevel, type.getDisplayName()));
    }

    public static KingdomPlayer newFirstJoinedPlayer(UUID id) {
        return new KingdomPlayer(id, new PlayerData(0));
    }
}
