package me.fourteendoggo.xkingdoms.storage;

public interface Repository<I, O> extends Iterable<O> {

    void add(I i);

    O get(I i);


}
