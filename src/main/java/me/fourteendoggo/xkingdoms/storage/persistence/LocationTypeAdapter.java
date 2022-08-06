package me.fourteendoggo.xkingdoms.storage.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.util.UUID;

public class LocationTypeAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter out, Location location) throws IOException {
        out.beginObject();
        assert location.getWorld() != null;
        out.name("worldID").value(location.getWorld().getUID().toString());
        out.name("x").value(location.getX());
        out.name("y").value(location.getY());
        out.name("z").value(location.getZ());
        out.name("yaw").value(location.getYaw());
        out.name("pitch").value(location.getPitch());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(in);

        UUID worldID = UUID.fromString(json.get("worldID").getAsString());
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float yaw = json.get("yaw").getAsFloat();
        float pitch = json.get("pitch").getAsFloat();

        return new Location(Bukkit.getWorld(worldID), x, y, z, yaw, pitch);
    }
}
