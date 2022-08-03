package me.fourteendoggo.xkingdoms.skill;

public abstract class Skill { // TODO: implement abilities
    private final SkillType type;
    private final int unlockLevel;

    public Skill(SkillType type, int unlockLevel) {
        this.type = type;
        this.unlockLevel = unlockLevel;
    }

    public SkillType getType() {
        return type;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public int getRemainingXP(SkillProgress progress) {
        int totalXP = getXPForLevel(progress.getLevel());
        return totalXP - progress.getXp();
    }

    // some formula probably
    public abstract int getXPForLevel(int level);
}
