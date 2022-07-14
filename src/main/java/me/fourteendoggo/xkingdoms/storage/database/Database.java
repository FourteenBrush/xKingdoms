package me.fourteendoggo.xkingdoms.storage.database;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.UUID;

public interface Database {

    void connect();

    void disconnect();

   KingdomPlayer loadPlayer(UUID id);

   void savePlayer(KingdomPlayer player);
}
