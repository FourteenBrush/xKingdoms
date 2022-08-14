package me.fourteendoggo.xkingdoms.storage.repository;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A cache impl which delays the removal of entries
 * Useful in situations like where a player leaves the server for a quick re-login and instead of saving the user
 * And loading it back from the database in a short period of time, it stays in the cache for a while
 * This class be handled as a normal collection, but the ::get operation is slightly optimized for situations as mentioned above
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

    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns if the cache contains this element
     * It will also remove any present removal queries for this entry
     * Assuming that when you actually want to get the entry, you don't call #contains and then #get
     * But just #get and check if it's notnull
     * @param key the key to search a value for
     * @return true if this entry exists, and it is not waiting for removal
     */
    public boolean contains(K key) {
        removals.remove(key);
        return map.containsKey(key);
    }

    /**
     * Gets the element from the cache, whether it's present or not
     * If you want to ensure that the element is not present in the removal queue
     * An additional call to ::put is or ::contains is required, as they both remove the removal query, ::put is preferred
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
        // if there was a remove query, we are sure that the element is present cuz removing the element creates a query
        // otherwise It's either present or not present, lets override the old element like the hashmap impl does
        if (removals.remove(key) == null) {
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
     * @return the value which was bound to the key, or null if there was no such entry
     */
    public V remove(K key) {
        // if the element wasn't already queried for removal, do that now
        removals.putIfAbsent(key, new RemoveQuery<>(map::remove, Instant.now().plus(removeDelay)));
        // return the value, like the hashmap impl does
        return get(key);
    }

    /**
     * @see Cache#iterator()
     * @return an iterator that iterates over the whole collection (pending removals included)
     */
    public Iterator<V> iterator() {
        return map.values().iterator();
    }

    private void startRemovalTask(XKingdoms plugin) {
        removalTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<K, RemoveQuery<K>>> iterator = removals.entrySet().iterator();
            Instant now = Instant.now();
            while (iterator.hasNext()) {
                Map.Entry<K, RemoveQuery<K>> entry = iterator.next();
                RemoveQuery<K> query = entry.getValue();
                // execute queries which should already be executed
                if (!query.executeTime().isBefore(now)) continue;
                query.function().accept(entry.getKey());
                iterator.remove();
            }
        }, 1L, 5 * 60 * 20L); // checks for queries to be executed every five minutes
    }

    /**
     * A query which, when executed, tells to remove an element of type <bold>K</bold> at a given time
     * There is no promise that the query will be run at that time, but it can't happen earlier
     * @param <K> the type of element which should be removed
     */
    private record RemoveQuery<K>(Consumer<K> function, Instant executeTime) {}
}
