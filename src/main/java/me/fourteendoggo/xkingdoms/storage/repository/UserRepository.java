package me.fourteendoggo.xkingdoms.storage.repository;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.Repository;
import me.fourteendoggo.xkingdoms.storage.DelayedRemovalCache;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;

public class UserRepository implements Repository<UUID, KingdomPlayer> {
    private final DelayedRemovalCache<UUID, KingdomPlayer> userMap;

    public UserRepository(XKingdoms plugin) {
        this.userMap = new DelayedRemovalCache<>(Duration.ofMinutes(10), plugin);
    }

    @Override
    public boolean has(UUID uuid) {
        return userMap.has(uuid);
    }

    @Override
    public KingdomPlayer get(UUID id) {
        return userMap.get(id);
    }

    public void put(KingdomPlayer player) {
        put(player.getUniqueId(), player);
    }

    @Override
    public void put(UUID id, KingdomPlayer player) {
        userMap.put(id, player);
    }

    public KingdomPlayer remove(UUID id, Runnable whenRemoved) {
        return userMap.remove(id, whenRemoved);
    }

    @Override
    public KingdomPlayer remove(UUID id) {
        return userMap.remove(id);
    }

    @NotNull
    @Override
    public Iterator<KingdomPlayer> iterator() {
        return userMap.values().iterator();
    }
}
