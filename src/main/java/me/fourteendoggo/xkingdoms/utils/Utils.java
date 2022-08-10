package me.fourteendoggo.xkingdoms.utils;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getCreatedFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                sneakyThrow(e);
            }
        }
        return file;
    }

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
