package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.skill.SkillData;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class PlayerData {
    private int level;
    private final SkillData skillData;
    private final Map<String, Home> homesMap;

    public PlayerData(int level, SkillData skillData, Collection<Home> homes) {
        this.level = level;
        this.skillData = skillData;
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

    public SkillData getSkillData() {
        return skillData;
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
