package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.UUID;

public interface PersistenceHandler {

    KingdomPlayer loadPlayer(UUID id);

    void savePlayer(KingdomPlayer player);
}
