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
     * @return the xp which needs to be gained before going to the next level
     */
    public abstract int getXPForLevel(int level);
}
