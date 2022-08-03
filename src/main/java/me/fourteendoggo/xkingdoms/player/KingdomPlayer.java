package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.skill.SkillData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class KingdomPlayer {
    private final UUID uuid;
    private Reference<Player> player;
    private final AtomicReference<PlayerData> playerData;

    public KingdomPlayer(UUID uuid, PlayerData data) {
        this.uuid = uuid;
        this.playerData = new AtomicReference<>(data);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerData getData() {
        return playerData.get();
    }

    public Player getPlayer() {
        return player.get();
    }

    public void login(Player bindTo) {
        this.player = new WeakReference<>(bindTo);
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
