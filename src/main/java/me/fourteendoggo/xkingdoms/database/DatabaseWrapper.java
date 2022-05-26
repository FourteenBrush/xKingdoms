package me.fourteendoggo.xkingdoms.database;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseWrapper {
    private final Database database;
    private final Logger logger;

    public DatabaseWrapper(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public boolean connect() {
        try {
            database.connect();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Tried to connect to the database but failed.", e);
        }
        return false;
    }

    public void disconnect() {
        try {
            database.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Tried to close the database but failed.", e);
        }
    }

    CompletableFuture<KingdomPlayer> loadPlayer(UUID id) {
        return makeFuture(() -> database.loadPlayer(id), "Failed to load player with uuid %s".formatted(id));
    }

    CompletableFuture<Void> savePlayer(KingdomPlayer player) {
        return makeFuture(() -> database.savePlayer(player), "Failed to save player with uuid %s".formatted(player.getId()));
    }

    private <T> CompletableFuture<T> makeFuture(Supplier<T> supplier, String errorMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                logger.log(Level.SEVERE, errorMessage, e);
                throw e;
            }
        });
    }

    private CompletableFuture<Void> makeFuture(Runnable runnable, String errorMessage) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.log(Level.SEVERE, errorMessage, e);
                throw e;
            }
        });
    }
}
