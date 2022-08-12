package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.skill.SkillProgress;
import me.fourteendoggo.xkingdoms.skill.SkillType;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private int level;
    private final Map<String, Home> homesMap = new HashMap<>();
    private final Map<SkillType, SkillProgress> skillsMap = new EnumMap<>(SkillType.class);

    public PlayerData(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addHome(Home home) {
        homesMap.put(home.name(), home);
    }

    public Home getHome(String name) {
        return homesMap.get(name);
    }

    public boolean hasHome(String name) {
        return homesMap.containsKey(name);
    }

    public void removeHome(String name) {
        homesMap.remove(name);
    }

    @UnmodifiableView
    public Map<String, Home> getHomes() {
        return Collections.unmodifiableMap(homesMap);
    }

    public void setSkillProgress(SkillType type, SkillProgress progress) {
        skillsMap.put(type, progress);
    }

    public SkillProgress getSkillProgress(SkillType type) {
        return skillsMap.get(type);
    }

    public void incrementSkillXP(SkillType type, int amount) {
        SkillProgress progress = skillsMap.computeIfAbsent(type, s -> new SkillProgress());
        progress.incrementXP(amount);
    }

    @UnmodifiableView
    public Map<SkillType, SkillProgress> getSkills() {
        return Collections.unmodifiableMap(skillsMap);
    }
}
