package me.fourteendoggo.xkingdoms.storage;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.database.DatabaseWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private final DatabaseWrapper database;
    private final Map<UUID, KingdomPlayer> userMap;

    public UserManager(DatabaseWrapper database) {
        this.database = database;
        this.userMap = new HashMap<>();
    }

    public void loadUser(UUID id) {
        database.loadUser(id).thenAccept(player -> userMap.put(id, player));
    }

    public void saveUser(UUID id) {
        KingdomPlayer user = getUser(id);
        database.saveUser(user).thenAccept(__ -> userMap.remove(id));
    }

    public KingdomPlayer getUser(UUID id) {
        return userMap.get(id);
    }


}
