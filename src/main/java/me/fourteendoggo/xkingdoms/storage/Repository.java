package me.fourteendoggo.xkingdoms.storage;

public interface Repository<K, V> extends Iterable<V> {

    V get(K k);

    void put(K k, V v);

    V remove(K k);
}
