package dev.grzybalson.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.*;
import java.util.function.*;

public final class TaskUtil implements AutoCloseable {
    private final JavaPlugin plugin;
    private final ExecutorService io;
    private volatile boolean closing;

    public TaskUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        this.io = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "HollyWallet-IO"); thread.setDaemon(true); return thread;
        });
    }
    public <T> void submit(Supplier<T> work, Consumer<T> success, Consumer<Throwable> failure) {
        if (closing) return;
        io.execute(() -> {
            T result;
            try { result = work.get(); }
            catch (Throwable exception) { main(() -> failure.accept(exception)); return; }
            main(() -> {
                try { success.accept(result); }
                catch (Throwable exception) { failure.accept(exception); }
            });
        });
    }
    public void main(Runnable work) {
        if (closing || !plugin.isEnabled()) return;
        try { Bukkit.getScheduler().runTask(plugin, work); }
        catch (org.bukkit.plugin.IllegalPluginAccessException ignored) { }
    }
    public void later(Runnable work) { main(work); }
    @Override public void close() {
        closing = true;
        io.shutdown();
        try {
            if (!io.awaitTermination(15, TimeUnit.SECONDS)) {
                io.shutdownNow();
                io.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException exception) { io.shutdownNow(); Thread.currentThread().interrupt(); }
    }
}
