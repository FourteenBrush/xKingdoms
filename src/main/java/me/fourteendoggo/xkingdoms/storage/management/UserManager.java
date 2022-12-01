package me.fourteendoggo.xkingdoms.storage.management;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.persistence.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private final Map<UUID, KingdomPlayer> userMap = new HashMap<>();
    private final Storage storage;

    public UserManager(Storage storage) {
        this.storage = storage;
    }

    public KingdomPlayer getUser(UUID id) {
        return userMap.get(id);
    }

    /**
     * Loads the player with the given uuid (synchronous operation) if not present in cache
     * @param id the uuid of the player
     * @return false if the player was not present in the cache and could not be loaded from the persistent storage, true otherwise
     */
    public boolean loadIfAbsent(UUID id) {
        return userMap.computeIfAbsent(id, storage::loadPlayerBlocking) != null;
    }

    public void unloadUser(UUID id) {
        KingdomPlayer kPlayer = userMap.remove(id);
        storage.savePlayer(kPlayer).thenRun(kPlayer::invalidate);
    }

    public void saveAllBlocking() {
        for (KingdomPlayer player : userMap.values()) {
            storage.savePlayerBlocking(player);
            player.invalidate();
        }
    }
}
