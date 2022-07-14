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

            UUID worldUUID = UUID.fromString(rs.getString("location_world"));
            double x = rs.getDouble("location_x");
            double y = rs.getDouble("location_y");
            double z = rs.getDouble("location_z");
            float yaw = rs.getFloat("location_yaw");
            float pitch = rs.getFloat("location_pitch");
            Location location = new Location(Bukkit.getWorld(worldUUID), x, y, z, yaw, pitch);

            return new Home(name, owner, location);
        } catch (SQLException e) {
            return null;
        }
    }
}
