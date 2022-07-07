package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.kingdom.Kingdom;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class PlayerData {
    private Kingdom kingdom;
    private final Map<String, Home> homesMap;

    public PlayerData(Set<Home> homes) {
        this.homesMap = new HashMap<>();
        for (Home home : homes) {
            homesMap.put(home.name(), home);
        }
    }

    public boolean hasHome(String name) {
        return homesMap.containsKey(name);
    }

    @UnmodifiableView
    public Map<String, Home> getHomes() {
        return Collections.unmodifiableMap(homesMap);
    }

    public Home getHome(String name) {
        return homesMap.get(name);
    }

    public void addHome(Home home) {
        homesMap.put(home.name(), home);
    }

    public boolean removeHome(String name) {
        return homesMap.remove(name) != null;
    }
}
