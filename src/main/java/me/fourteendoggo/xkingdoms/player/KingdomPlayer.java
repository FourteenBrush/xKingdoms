package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.skill.SkillData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

public class KingdomPlayer {
    private final UUID uuid;
    private final PlayerData playerData;

    public KingdomPlayer(UUID uuid, PlayerData playerData) {
        this.uuid = uuid;
        this.playerData = playerData;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerData getData() {
        return playerData;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void login() {
    }

    public void logout() {
    }

    public void levelUp(int reachedLevel) {
        Player player = getPlayer();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

        player.setLevel(reachedLevel);
    }

    public static KingdomPlayer newFirstJoinedPlayer(UUID id) {
        return new KingdomPlayer(id, new PlayerData(0, new SkillData(), Collections.emptyList()));
    }
}
