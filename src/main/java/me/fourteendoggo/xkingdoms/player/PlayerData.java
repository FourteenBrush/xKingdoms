package me.fourteendoggo.xkingdoms.player;

import me.fourteendoggo.xkingdoms.chat.ChatChannel;
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
    private ChatChannel joinedChannel;
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

    public void setJoinedChannel(ChatChannel joinedChannel) {
        this.joinedChannel = joinedChannel;
    }

    public boolean addHome(Home home) {
        return homesMap.putIfAbsent(home.name(), home) != null;
    }

    public Home removeHome(String name) {
        return homesMap.remove(name);
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

    public void updateSkillXp(SkillType type, int amount) {
        skillsMap.computeIfAbsent(type, t -> new SkillProgress()).incrementXP(amount);
    }

    @UnmodifiableView
    public Map<SkillType, SkillProgress> getSkills() {
        return Collections.unmodifiableMap(skillsMap);
    }
}
