package me.fourteendoggo.xkingdoms.skill;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.skills.WoodCuttingSkill;

import java.util.EnumMap;
import java.util.Map;

public class SkillsManager {
    private final Map<SkillType, Skill> skillsMap = new EnumMap<>(SkillType.class);

    public SkillsManager() {
        addSkill(new WoodCuttingSkill());
    }

    private void addSkill(Skill skill) {
        skillsMap.put(skill.getType(), skill);
    }

    public Skill getSkill(SkillType type) {
        return skillsMap.get(type);
    }

    public void checkProgress(SkillType type, KingdomPlayer player) {
        SkillData data = player.getData().getSkillData();
        int currentLevel = data.getLevel(type);
        if (currentLevel == -1) return;
        int currentXP = data.getCurrentXP(type);

        int totalXPForLevel = skillsMap.get(type).getXPForLevel(currentLevel);

        if (currentXP >= totalXPForLevel) {
            data.levelUp(type);
            player.levelUp(currentLevel + 1);
        }
    }
}
