package me.fourteendoggo.xkingdoms.utils;

public enum LangKey {
    NO_PERMISSION("no-permission");

    private final String path;

    LangKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
