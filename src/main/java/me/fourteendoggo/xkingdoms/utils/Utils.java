package me.fourteendoggo.xkingdoms.utils;

import org.bukkit.ChatColor;

public class Utils {
    public static final String MODERATOR_PERMISSION_STRING = "xkingdoms.moderator";

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
