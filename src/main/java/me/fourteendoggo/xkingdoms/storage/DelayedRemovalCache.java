package me.fourteendoggo.xkingdoms.storage;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * A cache impl which delays the actual removal of entries
 * Useful in situations like where a player leaves the server for a quick re-login and instead of saving the user
 * And loading it back from the database in a short period of time, it stays in the cache for a while
 * This can be handled as a normal collection, but the ::get operation is slightly optimized for situations as mentioned above
 * @param <K> the key type to store
 * @param <V> the according value type to store
 */
public class DelayedRemovalCache<K, V> {
    private final Duration removeDelay;
    private final XKingdoms plugin;
    private final Map<K, V> map;
    private final Map<K, RemoveQuery<K>> removals;
    private BukkitTask removalTask;

    public DelayedRemovalCache(Duration removeDelay, XKingdoms plugin) {
        this.removeDelay = removeDelay;
        this.plugin = plugin;
        this.map = new HashMap<>();
        this.removals = new HashMap<>();
    }

    /**
     * Gets the element from the cache, whether it's present or not
     * If you want to ensure that the element is not present in the removal queue, an additional call to ::put is required
     * @param key the key to search a value for
     * @return the element if found, null otherwise
     */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Puts the element in the cache
     * If the element was already queried for removal, this query is removed
     * @param key the key to put in the cache
     * @param value the according value
     */
    public void put(K key, V value) {
        // check if the entry was queried for removal and if so, undo that
        RemoveQuery<K> query = removals.remove(key);
        // if their was a remove query, we are sure that the element is present cuz removing the element creates a query
        // otherwise its either present or not present, lets override the old element like the hashmap impl does
        if (query == null) {
            map.put(key, value);
        }
        // start to run the removal task if this is the first element added, waste of memory to check an empty map otherwise
        if (removalTask == null) {
            startRemovalTask(plugin);
        }
    }

    /**
     * Queries the entry with this key for removal
     * This will be removed after the duration passed to the constructor
     * To prevent removal after calling ::remove, just call ::put again, which will simply remove the query
     * @param key the key to search a value for
     * @param whenRemoved an action to execute when the entry is actually removed from memory
     * @return the value which was bound to the key, or null if there was no such entry
     */
    public V remove(K key, Runnable whenRemoved) {
        Consumer<K> consumer = map::remove;
        consumer = consumer.andThen(k -> whenRemoved.run());
        return remove(key, consumer);
    }

    /**
     * @see DelayedRemovalCache#remove(K, Runnable)
     */
    public V remove(K key) {
        return remove(key, map::remove);
    }

    private V remove(K key, Consumer<K> consumer) {
        // if the element wasn't already queried for removal, do that now
        removals.putIfAbsent(key, new RemoveQuery<>(consumer, Instant.now().plus(removeDelay)));
        // return the value, like the hashmap impl does
        return get(key);
    }

    // support for Repository::iterator
    public Collection<V> values() {
        return map.values();
    }

    private void startRemovalTask(XKingdoms plugin) {
        removalTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<K, RemoveQuery<K>>> iterator = removals.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, RemoveQuery<K>> entry = iterator.next();
                RemoveQuery<K> query = entry.getValue();
                // execute queries which should already be executed
                if (query.executeTime().isBefore(Instant.now())) {
                    query.function().accept(entry.getKey());
                    iterator.remove();
                }
            }
        }, 1L, 5 * 60 * 20L); // checks for queries to be executed every five minutes
    }

    /**
     * A query which, when executed, tells to remove an element of type <bold>K</bold> at a given time
     * There is no promise that the query will be ran at that time, but it cant happen earlier
     * @param <K> the type of element which should be removed
     */
    private record RemoveQuery<K>(Consumer<K> function, Instant executeTime) {}
}
