package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.UUID;

public interface PersistenceHandler {

    void connect();

    void disconnect();

    KingdomPlayer loadPlayer(UUID id);

    void savePlayer(KingdomPlayer player);
}
