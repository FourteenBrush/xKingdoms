package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
    private final PersistenceHandler persistenceHandler;
    private final Logger logger;

    public Storage(PersistenceHandler persistenceHandler, Logger logger) {
        this.persistenceHandler = persistenceHandler;
        this.logger = logger;
    }

    /* synchronous operations */

    public boolean connect() {
        try {
            persistenceHandler.connect();
            if (persistenceHandler instanceof Database database) {
                database.executePatches(logger);
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to the database", e);
        }
        return false;
    }

    public void disconnect() {
        try {
            persistenceHandler.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    @Nullable
    public KingdomPlayer loadPlayerSync(UUID id) {
        try {
            KingdomPlayer player = persistenceHandler.loadPlayer(id);
            logger.info("Loaded the player with uuid " + id);
            return player;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load the player with uuid " + id, e);
        }
        return null;
    }

    public void savePlayerSync(KingdomPlayer player) {
        try {
            persistenceHandler.savePlayer(player);
            logger.info("Saved player " + player.getPlayer().getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save player " + player.getPlayer().getName(), e);
        }
    }

    /* asynchronous operations */

    public CompletableFuture<KingdomPlayer> loadPlayer(UUID id) {
        return makeFuture(() -> persistenceHandler.loadPlayer(id),
                "Loaded the player with uuid " + id,
                "Failed to load the player with uuid " + id);
    }

    public CompletableFuture<Void> savePlayer(KingdomPlayer player) {
        return makeFuture(() -> persistenceHandler.savePlayer(player),
                "Saved player " + player.getPlayer().getName(),
                "Failed to save player " + player.getPlayer().getName());
    }

    /* when things go wrong, only messaging the player is required, exception is handled here */

    /**
     * Creates a {@link CompletableFuture} from the given supplier
     * The database implementation we wrap doesn't do exception handling, we persistenceHandler it to the completable future
     * So this will handle possible exceptions that will occur and put them in the future object
     *
     * @param supplier the action to perform
     * @param logOnSuccess the message that needs to be logged when this operation completed successfully
     * @param logOnFailure the message that needs to be logged if an exception occurred while trying to compute the result
     * @param <T> the return type of the inner operation, this is the same type which the future will return
     * @return a future which either holds the return type of the operation, or a {@link Throwable} object
     */
    private <T> CompletableFuture<T> makeFuture(Supplier<T> supplier, String logOnSuccess, String logOnFailure) {
        return CompletableFuture.supplyAsync(supplier).whenComplete((t, throwable) -> {
            if (throwable != null) {
                logger.log(Level.SEVERE, logOnFailure, throwable);
            } else {
                logger.info(logOnSuccess);
            }
        });
    }

    /**
     * @see Storage#makeFuture(Supplier, String, String) but this uses a runnable
     */
    private CompletableFuture<Void> makeFuture(Runnable runnable, String logOnSuccess, String logOnFailure) {
        return CompletableFuture.runAsync(runnable).whenComplete((v, throwable) -> {
            if (throwable != null) {
                logger.log(Level.SEVERE, logOnFailure, throwable);
            } else {
                logger.info(logOnSuccess);
            }
        });
    }
}
