package me.fourteendoggo.xkingdoms.utils;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A promise that some code will finish its execution at some moment in time
 * Nearly the same thing as a {@link CompletableFuture}
 * This class also supports synchronous execution and has less memory overhead than a {@link CompletableFuture}
 * Async operations are ran on the bukkit's scheduler
 * @param <T> the return type of the future, might be {@link Void} when executing a {@link Runnable}
 */
public class ControlledAction<T> {
    private T result;
    private Exception thrownException;

    private void execute(Supplier<T> supplier) {
        try {
            result = supplier.get();
        } catch (Exception e) {
            thrownException = e;
        }
        completed();
    }

    private void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            thrownException = e;
        }
        completed();
    }

    private void completed() {

    }

    public static <T> ControlledAction<T> supplyAsync(XKingdoms plugin, Supplier<T> supplier) {
        return supplyAsync(supplier, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public static <T> ControlledAction<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        ControlledAction<T> action = new ControlledAction<>();
        executor.execute(() -> action.execute(supplier));
        return action;
    }

    public static ControlledAction<Void> runAsync(XKingdoms plugin, Runnable runnable) {
        return runAsync(runnable, newRunnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, newRunnable));
    }

    public static ControlledAction<Void> runAsync(Runnable runnable, Executor executor) {
        ControlledAction<Void> action = new ControlledAction<>();
        executor.execute(runnable);
        return action;
    }

    public static <T> ControlledAction<T> supply(Supplier<T> supplier) {
        return supplyAsync(supplier, Runnable::run);
    }

    public ControlledAction<T> whenCompleted(BiConsumer<T, Exception> onCompleteConsumer) {
        return this;
    }

    public ControlledAction<T> thenAccept(Consumer<T> resultConsumer) {
        return this;
    }

    public ControlledAction<T> thenRun(Runnable runnable) {
        return this;
    }

    private static abstract class Completion implements Runnable {
        private Completion next;
    }
}
