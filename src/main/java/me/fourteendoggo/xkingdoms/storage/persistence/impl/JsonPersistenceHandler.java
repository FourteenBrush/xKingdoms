package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.UUID;

public class JsonPersistenceHandler implements PersistenceHandler {
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Location.class, new LocationTypeAdapter())
            .registerTypeAdapter(Home.class, new HomeDeserializer())
            .create();
    private final XKingdoms plugin;

    public JsonPersistenceHandler(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    private File getPlayerDataFile(UUID playerId) {
        File dataFolder = new File(plugin.getDataFolder(), "playerdata");
        File playerFile = new File(dataFolder, playerId + ".json");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Could not create playerdata folder");
        }
        return playerFile;
    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        File playerFile = getPlayerDataFile(id);
        if (!playerFile.exists()) {
            return KingdomPlayer.newFirstJoinedPlayer(id);
        }
        try {
            String json = Files.readString(playerFile.toPath());
            return gson.fromJson(json, KingdomPlayer.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void savePlayer(KingdomPlayer player) {
        File playerFile = getPlayerDataFile(player.getUniqueId());
        String json = gson.toJson(player);
        try {
            Files.writeString(playerFile.toPath(), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LocationTypeAdapter extends TypeAdapter<Location> {

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

    private static class HomeDeserializer implements JsonDeserializer<Home> {

        @Override
        public Home deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = (JsonObject) json;
            String name = obj.get("name").getAsString();
            UUID owner = UUID.fromString(obj.get("owner").getAsString());
            Location location = context.deserialize(obj.get("location"), Location.class);

            return new Home(name, owner, location);
        }
    }
}
