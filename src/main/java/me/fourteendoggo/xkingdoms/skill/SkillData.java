package me.fourteendoggo.xkingdoms.skill;

import java.util.EnumMap;
import java.util.Map;

public class SkillData {
    private final Map<SkillType, SkillProgress> skillsMap = new EnumMap<>(SkillType.class);

    public int getLevel(SkillType type) {
        SkillProgress progress = skillsMap.get(type);
        return progress != null ? progress.getLevel() : -1;
    }

    public int getCurrentXP(SkillType type) {
        SkillProgress progress = skillsMap.get(type);
        return progress != null ? progress.getXp() : -1;
    }

    public void levelUp(SkillType type) {
        SkillProgress progress = skillsMap.computeIfAbsent(type, s -> new SkillProgress());
        progress.setLevel(progress.getLevel() + 1);
        progress.setXp(0);
    }

    public void incrementXP(SkillType type, int amount) {
        SkillProgress progress = skillsMap.computeIfAbsent(type, s -> new SkillProgress());
        progress.setXp(progress.getXp() + amount);
    }

    public void incrementLevel(SkillType type, int amount) {
        SkillProgress progress = skillsMap.computeIfAbsent(type, s -> new SkillProgress());
        progress.setLevel(progress.getLevel() + amount);
    }

    public void setProgress(SkillType type, SkillProgress progress) {
        skillsMap.put(type, progress);
    }
}
