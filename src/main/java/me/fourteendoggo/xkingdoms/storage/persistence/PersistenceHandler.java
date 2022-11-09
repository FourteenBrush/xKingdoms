package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.utils.Home;

import java.util.UUID;

public interface PersistenceHandler {

    KingdomPlayer loadPlayer(UUID id);

    void deleteHome(Home home);

    void savePlayer(KingdomPlayer player);
}
