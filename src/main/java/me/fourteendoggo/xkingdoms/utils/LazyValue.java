package me.fourteendoggo.xkingdoms.utils;

import java.util.Optional;

public class LazyValue<T> {
    private final ThrowingSupplier<T> supplier;
    private T value;

    public LazyValue(ThrowingSupplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (value == null) {
            try {
                value = supplier.get();
            } catch (Exception e) {
                Utils.sneakyThrow(e);
            }
        }
        return value;
    }

    public Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
