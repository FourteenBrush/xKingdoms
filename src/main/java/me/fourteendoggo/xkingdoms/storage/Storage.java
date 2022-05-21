package me.fourteendoggo.xkingdoms.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.UUID;

public interface Storage {

    void connect();

    void disconnect();

    HikariDataSource initDataSource(Xkingdoms plugin);

    KingdomPlayer createPlayer(UUID id);

    KingdomPlayer loadPlayer(UUID id);

    void updatePlayer(KingdomPlayer player);

}
