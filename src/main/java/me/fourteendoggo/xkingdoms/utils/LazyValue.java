package me.fourteendoggo.xkingdoms.utils;

public class LazyValue<T, E extends Exception> {
    private final ThrowingSupplier<T, E> valueSupplier;
    private T value;

    public LazyValue(ThrowingSupplier<T, E> valueSupplier) {
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

    // for gc purposes
    public void clear() {
        value = null;
    }
}
