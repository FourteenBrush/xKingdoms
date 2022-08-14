package me.fourteendoggo.xkingdoms.skill.skills;

import me.fourteendoggo.xkingdoms.skill.Skill;
import me.fourteendoggo.xkingdoms.skill.SkillType;

public class MiningSkill extends Skill {

    public MiningSkill() {
        super(SkillType.MINING, 0);
    }

    @Override
    public int getRequiredXp(int level) {
        return 5 + level;
    }
}
