package me.fourteendoggo.xkingdoms.skills;

import java.util.Objects;

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

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillProgress that = (SkillProgress) o;
        return level == that.level && xp == that.xp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, xp);
    }
}
