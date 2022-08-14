package me.fourteendoggo.xkingdoms.storage.repository.impl;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.repository.Cache;
import me.fourteendoggo.xkingdoms.storage.repository.DelayedRemovalCache;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;

public class UserCache implements Cache<UUID, KingdomPlayer> {
    private final DelayedRemovalCache<UUID, KingdomPlayer> userMap;

    public UserCache(XKingdoms plugin) {
        this.userMap = new DelayedRemovalCache<>(Duration.ofMinutes(10), plugin);
    }

    @Override
    public boolean isEmpty() {
        return userMap.isEmpty();
    }

    @Override
    public boolean contains(UUID uuid) {
        return userMap.contains(uuid);
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

    @Override
    public KingdomPlayer remove(UUID id) {
        return userMap.remove(id);
    }

    @NotNull
    @Override
    public Iterator<KingdomPlayer> iterator() {
        return userMap.iterator();
    }
}
