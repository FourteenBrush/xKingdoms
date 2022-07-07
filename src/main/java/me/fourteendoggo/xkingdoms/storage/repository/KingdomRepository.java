package me.fourteendoggo.xkingdoms.storage.repository;

import me.fourteendoggo.xkingdoms.kingdom.Kingdom;
import me.fourteendoggo.xkingdoms.storage.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KingdomRepository implements Repository<String, Kingdom> {
    private final Map<String, Kingdom> kingdomMap = new HashMap<>();

    @Override
    public boolean has(String name) {
        return kingdomMap.containsKey(name);
    }

    @Override
    public Kingdom get(String name) {
        return kingdomMap.get(name);
    }

    public void put(Kingdom kingdom) {
        put(kingdom.getName(), kingdom);
    }

    @Override
    public void put(String name, Kingdom kingdom) {
        kingdomMap.put(name, kingdom);
    }

    @Override
    public Kingdom remove(String name) {
        return kingdomMap.remove(name);
    }

    @NotNull
    @Override
    public Iterator<Kingdom> iterator() {
        return kingdomMap.values().iterator();
    }
}
