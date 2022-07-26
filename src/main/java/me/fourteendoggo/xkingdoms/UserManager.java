package me.fourteendoggo.xkingdoms;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.database.DatabaseWrapper;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private final DatabaseWrapper database;
    private final Map<UUID, KingdomPlayer> playerMap = new HashMap<>();

    public UserManager(DatabaseWrapper database) {
        this.database = database;
    }

    public void loadUserSync(UUID id) {
        playerMap.computeIfAbsent(id, database::loadPlayerSync);
    }

    public void loginUser(Player player) {
        playerMap.get(player.getUniqueId()).login(player);
    }

    public void unloadUser(UUID id) {

    }
}
