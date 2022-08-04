package me.fourteendoggo.xkingdoms.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class KingdomPlayerAdapter extends TypeAdapter<KingdomPlayer> {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(KingdomPlayer.class, this).create();

    @Override
    public void write(JsonWriter writer, KingdomPlayer player) throws IOException {
        writer.beginObject();

        writer.name("uuid").value(player.getUniqueId().toString());

        // writer.name("playerData");
        // writer.beginObject();

        // PlayerData data = player.getData();
        // String playerData = gson.toJson(data);
        // System.out.println("Debuug: " + playerData);

        // writer.value(playerData);
        // writer.endObject();

        writer.endObject();
    }

    @Override
    public KingdomPlayer read(JsonReader reader) {
        return gson.fromJson(reader, KingdomPlayer.class);
    }
}
