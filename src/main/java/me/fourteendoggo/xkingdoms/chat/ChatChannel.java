package me.fourteendoggo.xkingdoms.chat;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.HashSet;
import java.util.Set;

public class ChatChannel {
    private final String name;
    private final String joinPermission;
    private final Set<KingdomPlayer> joinedPlayers;

    public ChatChannel(String name, String joinPermission) {
        this.name = name;
        this.joinPermission = joinPermission;
        this.joinedPlayers = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getJoinPermission() {
        return joinPermission;
    }

    public void join(KingdomPlayer player) {
        joinedPlayers.add(player);
        player.getData().setJoinedChannel(this);
    }

    public void leave(KingdomPlayer player) {
        joinedPlayers.remove(player);
        player.getData().setJoinedChannel(null);
    }
}
