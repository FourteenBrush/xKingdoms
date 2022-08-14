package me.fourteendoggo.xkingdoms.utils;

public class LazyValue<T> {
    private final ThrowingSupplier<T> valueSupplier;
    private T value;

    public LazyValue(ThrowingSupplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public T get() {
        if (value == null) {
            try {
                value = valueSupplier.get();
            } catch (Exception e) {
                Utils.sneakyThrow(e);
            }
        }
        return value;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
