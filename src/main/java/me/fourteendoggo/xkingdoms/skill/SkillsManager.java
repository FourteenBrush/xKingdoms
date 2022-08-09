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

    public void handleLevelling(SkillType type, KingdomPlayer player) {
        SkillProgress progress = player.getData().getSkillProgress(type);
        if (progress == null) return;

        Skill skill = skillsMap.get(type);
        int level = progress.getLevel();
        int xp = progress.getXp(); // the xp for this level
        int requiredXP = skill.getXPForLevel(level);

        if (xp >= requiredXP) {
            progress.incrementLevel();
            progress.setXp(xp - requiredXP);
            player.levelUpSkill(type, level + 1);
        } else {
            player.showProgress(type, xp, requiredXP);
            // player.getPlayer().sendMessage(ChatColor.YELLOW + "# " + ChatColor.GRAY + "%s XP: %s/%s".formatted(
            //                    type.getDisplayName(),
            //                    xp, requiredXP
            //            ));
        }
    }
}
