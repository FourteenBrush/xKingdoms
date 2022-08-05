package me.fourteendoggo.xkingdoms.skill;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.skills.FarmingSkill;
import me.fourteendoggo.xkingdoms.skill.skills.WoodCuttingSkill;

import java.util.EnumMap;
import java.util.Map;

public class SkillsManager {
    private final Map<SkillType, Skill> skillsMap = new EnumMap<>(SkillType.class);

    public SkillsManager() {
        addSkill(new WoodCuttingSkill());
        addSkill(new FarmingSkill());
    }

    private void addSkill(Skill skill) {
        skillsMap.put(skill.getType(), skill);
    }

    public Skill getSkill(SkillType type) {
        return skillsMap.get(type);
    }

    public void checkProgress(SkillType type, KingdomPlayer player) {
        SkillData skillData = player.getData().getSkillData();
        SkillProgress progress = skillData.getProgress(type);
        if (progress == null) return;

        Skill skill = skillsMap.get(type);
        int level = progress.getLevel();
        int xp = progress.getXp();
        int xpThatMustBeGained = skill.getXPForLevel(level);

        if (xp >= xpThatMustBeGained) {
            progress.incrementLevel();
            progress.setXp(xp - xpThatMustBeGained);
            player.levelUp(level + 1);
        }
    }
}
