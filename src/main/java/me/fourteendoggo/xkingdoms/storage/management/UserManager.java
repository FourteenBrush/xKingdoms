package me.fourteendoggo.xkingdoms.storage.management;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.persistence.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Iterable<KingdomPlayer> {
    private final Map<UUID, KingdomPlayer> userMap = new HashMap<>();
    private final Storage storage;

    public UserManager(Storage storage) {
        this.storage = storage;
    }

    @NotNull
    @Override
    public Iterator<KingdomPlayer> iterator() {
        return userMap.values().iterator();
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
        return userMap.computeIfAbsent(id, storage::loadPlayerSync) != null;
    }

    public void unloadUser(UUID id) {
        KingdomPlayer kPlayer = userMap.remove(id);
        storage.savePlayer(kPlayer).thenRun(kPlayer::invalidate);
    }

    public void saveAll() {
        userMap.values().forEach(storage::savePlayerSync);
    }
}
