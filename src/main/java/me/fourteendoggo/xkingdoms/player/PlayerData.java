package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.kingdom.Kingdom;
import me.fourteendoggo.xkingdoms.utils.Home;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PlayerData {
    private Kingdom kingdom;
    private final Map<String, Home> homes;

    public PlayerData(Set<Home> homes) {
        this.homes = new HashMap<>();
        for (Home home : homes) {
            this.homes.put(home.name(), home);
        }
    }

    public Optional<Home> getHome(String name) {
        return Optional.ofNullable(homes.get(name));
    }

    public boolean addHome(Home home) {
        return homes.put(home.name(), home) == null;
    }
}
