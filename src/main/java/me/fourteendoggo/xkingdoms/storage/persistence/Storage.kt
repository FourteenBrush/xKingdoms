package me.fourteendoggo.xkingdoms.storage.persistence

import me.fourteendoggo.xkingdoms.utils.Home
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger

class Storage(private val persistenceHandler: PersistenceHandler, private val logger: Logger) {

    /* synchronous operations */
    fun connect(): Boolean {
        try {
            if (persistenceHandler is Database) {
                persistenceHandler.connect()
                return persistenceHandler.executePatches(logger)
            }
            return true
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to connect to the database", e)
        }
        return false
    }

    fun disconnect() {
        try {
            if (persistenceHandler is Database) {
                persistenceHandler.disconnect()
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to close database connection", e)
        }
    }

    fun loadPlayerBlocking(id: UUID): KingdomPlayer? {
        try {
            val player = persistenceHandler.loadPlayer(id)
            logger.info("Loaded the player with uuid $id")
            return player
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to load the player with uuid $id", e)
        }
        return null
    }

    fun savePlayerBlocking(player: KingdomPlayer) {
        try {
            persistenceHandler.savePlayer(player)
            logger.info("Loaded player " + player.player.name)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to load player " + player.player.name, e)
        }
    }

    /* asynchronous operations */
    fun loadPlayer(id: UUID): CompletableFuture<KingdomPlayer?> = makeFuture<KingdomPlayer?>(
            { persistenceHandler.loadPlayer(id) },
            "Loaded the player with uuid $id",
            "Failed to load the player with uuid $id")

    fun deleteHome(home: Home): CompletableFuture<Void> = makeFuture(
            { persistenceHandler.deleteHome(home) },
            "Deleted home " + home.name + " for " + home.owner,
            "Failed to delete home " + home.name + " for " + home.owner)

    fun savePlayer(player: KingdomPlayer): CompletableFuture<Void> = makeFuture(
            { persistenceHandler.savePlayer(player) },
            "Saved player " + player.player.name,
            "Failed to save player " + player.player.name)
    /* when things go wrong, only messaging the player is required, exception is handled here */
    /**
     * Creates a [CompletableFuture] from the given supplier
     * The database implementation we wrap doesn't do exception handling, we delegate it to the completable future
     * So this will handle possible exceptions that will occur and put them in the future object
     *
     * @param supplier     the action to perform
     * @param logOnSuccess the message that needs to be logged when this operation completed successfully
     * @param logOnFailure the message that needs to be logged if an exception occurred while trying to compute the result
     * @param <T>          the return type of the inner operation, this is the same type which the future will return
     * @return a future which either holds the return type of the operation, or a [Throwable] object
    </T> */
    private fun <T> makeFuture(supplier: Supplier<T>, logOnSuccess: String, logOnFailure: String): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(supplier).whenComplete { t: T, throwable: Throwable? ->
            if (throwable != null) {
                logger.log(Level.SEVERE, logOnFailure, throwable)
            } else {
                logger.info(logOnSuccess)
            }
        }
    }

    /**
     * @see Storage.makeFuture
     */
    private fun makeFuture(runnable: Runnable, logOnSuccess: String, logOnFailure: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync(runnable).whenComplete { v: Void?, throwable: Throwable? ->
            if (throwable != null) {
                logger.log(Level.SEVERE, logOnFailure, throwable)
            } else {
                logger.info(logOnSuccess)
            }
        }
    }
}
