package me.fourteendoggo.xkingdoms.utils;

import org.bukkit.ChatColor;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
