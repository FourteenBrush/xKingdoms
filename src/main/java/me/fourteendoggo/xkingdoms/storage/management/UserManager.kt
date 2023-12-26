package me.fourteendoggo.xkingdoms.storage.management

import me.fourteendoggo.xkingdoms.player.KingdomPlayer
import me.fourteendoggo.xkingdoms.storage.persistence.Storage
import java.util.*

class UserManager(private val storage: Storage) : Iterable<KingdomPlayer> {
    private val userMap = HashMap<UUID, KingdomPlayer>()

    override fun iterator() = userMap.values.iterator()

    fun getUser(id: UUID) = userMap[id]

    /**
     * Loads the player with the given uuid (synchronous operation) if not present in cache
     * @param id the uuid of the player
     * @return false if the player was not present in the cache and could not be loaded from the persistent storage, true otherwise
     */
    fun loadIfAbsent(id: UUID): Boolean {
        // TODO: check if this works, kotlin doesn't have (K) -> V? semantics for computeIfAbsent
        @Suppress("UNCHECKED_CAST")
        val uncheckedMap = userMap as HashMap<UUID, KingdomPlayer?>
        return uncheckedMap.computeIfAbsent(id) { storage.loadPlayerBlocking(id) } != null
    }

    fun unloadUser(id: UUID) {
        val kPlayer = userMap.remove(id)
        storage.savePlayer(kPlayer).thenRun { kPlayer!!.invalidate() }
    }

    fun saveAllBlocking() {
        for (player in userMap.values) {
            storage.savePlayerBlocking(player)
            player.invalidate()
        }
    }
}
