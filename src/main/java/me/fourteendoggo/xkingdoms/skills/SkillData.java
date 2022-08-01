package me.fourteendoggo.xkingdoms.skills;

import java.util.IdentityHashMap;
import java.util.Map;

public class SkillData {
    // TODO make this a HashMap<Skill, SkillProgress>
    private final Map<SkillType, SkillProgress> skillsMap = new IdentityHashMap<>();

    public int getLevel(SkillType skill) {
        SkillProgress progress = skillsMap.get(skill);
        return progress != null ? progress.getLevel() : -1;
    }

    public int getCurrentXP(SkillType skill) {
        SkillProgress progress = skillsMap.get(skill);
        return progress != null ? progress.getXp() : -1;
    }

    public void incrementXP(SkillType skill, int amount) {
        SkillProgress progress = skillsMap.computeIfAbsent(skill, s -> new SkillProgress());
        progress.setXp(progress.getXp() + amount);
    }

    public void incrementLevel(SkillType skill, int amount) {
        SkillProgress progress = skillsMap.computeIfAbsent(skill, s -> new SkillProgress());
        progress.setLevel(progress.getLevel() + amount);
    }
}
