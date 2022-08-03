package me.fourteendoggo.xkingdoms.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record Home(String name, UUID owner, Location location) {

    public static Home fromResultSet(ResultSet rs) {
        try {
            String name = rs.getString("name");
            UUID owner = UUID.fromString(rs.getString("owner"));

            UUID worldUUID = UUID.fromString(rs.getString("world"));
            double x = rs.getDouble("x");
            double y = rs.getDouble("y");
            double z = rs.getDouble("z");
            float yaw = rs.getFloat("yaw");
            float pitch = rs.getFloat("pitch");
            Location location = new Location(Bukkit.getWorld(worldUUID), x, y, z, yaw, pitch);

            return new Home(name, owner, location);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
