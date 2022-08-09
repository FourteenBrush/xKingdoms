package me.fourteendoggo.xkingdoms.skill;

public class SkillProgress {
    private int level;
    private int xp;

    public SkillProgress() {
        this(0, 0);
    }

    public SkillProgress(int level, int xp) {
        this.level = level;
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void incrementLevel() {
        level++;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void incrementXP(int amount) {
        xp += amount;
    }
}
