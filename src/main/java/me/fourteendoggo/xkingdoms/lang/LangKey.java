package me.fourteendoggo.xkingdoms.lang;

public enum LangKey {
    RELOAD_FINISHED("reload-finished"),
    JOIN_MESSAGE("join-message.normal"),
    JOIN_VANISHED("join-message.vanished"),
    QUIT_MESSAGE("quit-message.normal"),
    QUIT_VANISHED("quit-message.vanished"),
    HOME_LIMIT_REACHED("home.limit-reached"),
    HOME_ALREADY_EXISTS("home.already-exists"),
    HOME_NO_HOMES_CREATED("home.no-homes-created"),
    HOME_CREATED("home.created"),
    VANISH_ANNOUNCE("vanish.announce-to-mods"),
    VANISH_BACK_VISIBLE_ANNOUNCE("vanish.back-visible"),
    VANISH_ENABLED("vanish.enabled.for-self"),
    VANISH_ENABLED_FOR_OTHER("vanish.enabled.for-other"),
    VANISH_ENABLED_BY_OTHER("vanish.enabled.by-other"),
    VANISH_DISABLED("vanish.disabled.for-self"),
    VANISH_DISABLED_FOR_OTHER("vanish.disabled.for-other"),
    VANISH_DISABLED_BY_OTHER("vanish.disabled.by-other"),
    VANISH_NOBODY_VANISHED("vanish.nobody-vanished"),
    VANISH_ALREADY_VANISHED("vanish.already-vanished.self"),
    VANISH_ALREADY_VANISHED_OTHER("vanish.already-vanished.other"),
    VANISH_ALREADY_VISIBLE("vanish.already-visible.self"),
    VANISH_OTHER_ALREADY_VISIBLE("vanish.already-visible.other");

    private final String path;

    LangKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
