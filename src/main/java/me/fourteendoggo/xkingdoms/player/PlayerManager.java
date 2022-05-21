package me.fourteendoggo.xkingdoms.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, KingdomPlayer> players = new HashMap<>();

    public KingdomPlayer getPlayer(UUID id) {
        return this.players.get(id);
    }

    public void insertPlayer(KingdomPlayer player) {
        this.players.putIfAbsent(player.getId(), player);
    }
}
