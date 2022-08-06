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

    /**
     * Gets the required xp to level up, not the xp that needs to be gained in the level before, this is incremental
     * @return the xp that is required to be able to go to the next level
     */
    public abstract int getXPForLevel(int level);
}
