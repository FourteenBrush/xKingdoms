package me.fourteendoggo.xkingdoms.storage.repository;

public interface Cache<K, V> extends Iterable<V> {

    boolean isEmpty();

    boolean contains(K k);

    V get(K k);

    void put(K k, V v);

    V remove(K k);
}
