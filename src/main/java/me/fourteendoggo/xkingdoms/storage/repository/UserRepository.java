package me.fourteendoggo.xkingdoms.storage.repository;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserRepository implements Iterable<KingdomPlayer> {
    private final UserFactory factory;
    private final Map<UUID, KingdomPlayer> userMap;

    public UserRepository(UserFactory factory) {
        this.factory = factory;
        this.userMap = new HashMap<>();
    }

    public void add(UUID id) {
        factory.newUser(id).thenAccept(player -> userMap.put(id, player));
    }

    public Optional<KingdomPlayer> getIfPresent(UUID id) {
        return Optional.ofNullable(userMap.get(id));
    }

    public KingdomPlayer remove(UUID id) {
        return userMap.remove(id);
    }

    @NotNull
    @Override
    public Iterator<KingdomPlayer> iterator() {
        return userMap.values().iterator();
    }


    @FunctionalInterface
    public interface UserFactory {

        CompletableFuture<KingdomPlayer> newUser(UUID id);
    }
}
