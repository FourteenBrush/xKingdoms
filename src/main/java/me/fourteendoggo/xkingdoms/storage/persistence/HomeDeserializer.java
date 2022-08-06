package me.fourteendoggo.xkingdoms.storage.persistence;

import com.google.gson.*;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.UUID;

public class HomeDeserializer implements JsonDeserializer<Home> {

    @Override
    public Home deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = (JsonObject) json;
        String name = obj.get("name").getAsString();
        UUID owner = UUID.fromString(obj.get("owner").getAsString());
        Location location = context.deserialize(obj.get("location"), Location.class);

        return new Home(name, owner, location);
    }
}
