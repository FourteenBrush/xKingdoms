package me.fourteendoggo.xkingdoms.player;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class KingdomPlayer {
    private final UUID uuid;
    private final AtomicReference<PlayerData> playerdata;

    public KingdomPlayer(UUID uuid, PlayerData data) {
        this.uuid = uuid;
        this.playerdata = new AtomicReference<>(data);
        Double.NaN
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerData getData() {
        return playerdata.get();
    }
}
