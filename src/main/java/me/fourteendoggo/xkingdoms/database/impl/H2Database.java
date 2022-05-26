package me.fourteendoggo.xkingdoms.database.impl;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.database.ConnectionProvider;
import me.fourteendoggo.xkingdoms.database.Database;

import java.util.UUID;

public class H2Database implements Database {
    private final ConnectionProvider connProvider; // TODO let wrapper handle the Connectionprovider::testConn instead of this

    public H2Database(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return null;
    }

    @Override
    public void savePlayer(KingdomPlayer player) {

    }
}
