package me.fourteendoggo.xkingdoms.skill.skills;

import me.fourteendoggo.xkingdoms.skill.Skill;
import me.fourteendoggo.xkingdoms.skill.SkillType;

public class FarmingSkill extends Skill {

    public FarmingSkill() {
        super(SkillType.FARMING, 0);
    }

    @Override
    public int getRequiredXp(int level) {
        return 15;
    }
}
