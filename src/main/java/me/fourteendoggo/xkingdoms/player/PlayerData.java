package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.kingdom.Kingdom;
import me.fourteendoggo.xkingdoms.utils.Home;

import java.util.*;

public class PlayerData {
    private Kingdom kingdom;
    private final Map<String, Home> homesMap;

    public PlayerData(Set<Home> homes) {
        this.homesMap = new HashMap<>();
        homes.forEach(home -> homesMap.put(home.name(), home));
    }

    public Home getHome(String name) {
        return homesMap.get(name);
    }

    public boolean addHome(Home home) {
        return homesMap.put(home.name(), home) == null;
    }

    public boolean removeHome(String name) {
        return homesMap.remove(name) != null;
    }
}
