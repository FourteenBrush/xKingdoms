package me.fourteendoggo.xkingdoms.player;

import org.bukkit.entity.Player;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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

    public void login(Player bindTo) {
        this.player = new WeakReference<>(bindTo);
    }

    public void logout() {
    }

    public Player getPlayer() {
        return player.get();
    }

    public boolean isModerator() {
        return getPlayer().hasPermission("xkingdoms.moderator");
    }
}
