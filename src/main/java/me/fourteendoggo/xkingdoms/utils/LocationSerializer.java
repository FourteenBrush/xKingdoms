package me.fourteendoggo.xkingdoms.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Location;

import java.lang.reflect.Type;

public class LocationSerializer implements JsonSerializer<Location> {
    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        assert src.getWorld() != null;
        json.addProperty("worldID", src.getWorld().getUID().toString());
        json.addProperty("x", src.getX());
        json.addProperty("y", src.getY());
        json.addProperty("z", src.getZ());
        json.addProperty("yaw", src.getYaw());
        json.addProperty("pitch", src.getPitch());

        return json;
    }
}
