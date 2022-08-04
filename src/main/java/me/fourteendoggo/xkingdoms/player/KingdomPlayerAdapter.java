package me.fourteendoggo.xkingdoms.player;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class KingdomPlayerAdapter extends TypeAdapter<KingdomPlayer> {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(KingdomPlayer.class, this).create();

    @Override
    public void write(JsonWriter writer, KingdomPlayer player) throws IOException {
        writer.beginObject();

        writer.name("uuid").value(player.getUniqueId().toString());

        writer.name("playerData");
        writer.beginObject();

        PlayerData data = player.getData();
        String playerData = gson.toJson(data, PlayerData.class);
        System.out.println("Debug: " + playerData);

        writer.value(playerData);
        writer.endObject();

        writer.endObject();
    }

    @Override
    public KingdomPlayer read(JsonReader reader) {
        return gson.fromJson(reader, KingdomPlayer.class);
    }

    public static class PlayerDataSerializer implements JsonSerializer<KingdomPlayer> {
        private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        @Override
        public JsonElement serialize(KingdomPlayer player, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", player.getUniqueId().toString());
            json.addProperty("playerData", gson.toJson(player.getData(), PlayerData.class));

            return json;
        }
    }
}
