package me.fourteendoggo.xkingdoms.skill;

public enum SkillType {
    FARMING("Farming"),
    WOODCUTTING("Wood cutting"),
    MINING("Mining");

    private final String displayName;

    SkillType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
