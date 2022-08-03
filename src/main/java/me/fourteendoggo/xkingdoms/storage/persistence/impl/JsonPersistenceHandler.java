package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class JsonPersistenceHandler implements PersistenceHandler {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
}
