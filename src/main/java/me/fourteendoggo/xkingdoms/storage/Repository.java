package me.fourteendoggo.xkingdoms.storage;

public interface Repository<K, V> extends Iterable<V> {

    boolean has(K k);

    V get(K k);

    void put(K k, V v);

    V remove(K k);
}
