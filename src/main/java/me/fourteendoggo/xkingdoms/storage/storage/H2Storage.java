package me.fourteendoggo.xkingdoms.storage.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.fourteendoggo.xkingdoms.Xkingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.storage.Storage;

import java.util.UUID;

public class H2Storage implements Storage {

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public HikariDataSource initDataSource(Xkingdoms plugin) {
        return null;
    }

    @Override
    public KingdomPlayer createPlayer(UUID id) {
        return null;
    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return null;
    }

    @Override
    public void updatePlayer(KingdomPlayer player) {

    }
}
