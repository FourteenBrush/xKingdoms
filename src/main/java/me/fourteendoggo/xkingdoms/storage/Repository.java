package me.fourteendoggo.xkingdoms.storage;

public interface Repository<K, V> extends Iterable<V> {

    boolean contains(K k);

    V get(K k);

    void put(K k, V v);

    V remove(K k);
}
