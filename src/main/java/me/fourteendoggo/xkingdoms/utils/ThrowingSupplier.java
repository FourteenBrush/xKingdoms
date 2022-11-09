package me.fourteendoggo.xkingdoms.utils;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

    T get() throws E;
}
