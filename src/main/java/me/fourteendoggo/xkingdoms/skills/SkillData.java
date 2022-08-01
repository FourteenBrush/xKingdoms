package me.fourteendoggo.xkingdoms.skills;

import java.util.IdentityHashMap;
import java.util.Map;

public class SkillData {
    private final Map<SkillType, SkillRecord> skillsMap = new IdentityHashMap<>();

    public int getLevel(SkillType skill) {
        SkillRecord data = skillsMap.get(skill);
        return data != null ? data.level() : -1;
    }

    public int getCurrentXP(SkillType skill) {
        SkillRecord data = skillsMap.get(skill);
        return data != null ? data.currentXP() : -1;
    }

    public void incrementXP(SkillType skill, int amount) {
        SkillRecord data = skillsMap.computeIfAbsent(skill, s -> new SkillRecord(0, 0));

    }

    private record SkillRecord(int currentXP, int level) {}

    private static class SkillSnapshot {
        private int level;
        private int currentXP;

        public SkillSnapshot() {
            this(0, 0);
        }

        public SkillSnapshot(int level, int currentXP) {
            this.level = level;
            this.currentXP = currentXP;
        }

        public
    }
}
