package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.utils.Home;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class PlayerData {
    private int level;
    private final Map<String, Home> homesMap;

    public PlayerData(int level, Collection<Home> homes) {
        this.level = level;
        this.homesMap = new HashMap<>();

        for (Home home : homes) {
            homesMap.put(home.name(), home);
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
